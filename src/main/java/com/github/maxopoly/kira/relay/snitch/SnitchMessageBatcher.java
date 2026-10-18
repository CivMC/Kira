package com.github.maxopoly.kira.relay.snitch;

import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongSupplier;

// Collects snitch hits per Discord channel and sends them as one message.
public final class SnitchMessageBatcher {

	static final long COLLECT_WINDOW_MILLIS = 1000;
	static final int MAX_ATTEMPTS = 3;
	static final long RETRY_DELAY_MILLIS = 2_000;
	private final SnitchMessageSender sender;
	private final Scheduler scheduler;
	private final LongSupplier clock;
	private final Logger logger;
	private final Random random = new Random();
	private final Object lock = new Object();
	private final Map<Long, Buffer> buffers = new HashMap<>();
	private boolean running = true;
	public SnitchMessageBatcher(SnitchMessageSender sender, Scheduler scheduler, LongSupplier clock, Logger logger) {
		this.sender = sender;
		this.scheduler = scheduler;
		this.clock = clock;
		this.logger = logger;
	}

	public static SnitchMessageBatcher create(SnitchMessageSender sender, Logger logger) {
		return new SnitchMessageBatcher(sender, new ExecutorScheduler(logger), System::currentTimeMillis, logger);
	}

	private static Throwable unwrap(Throwable failure) {
		return failure instanceof CompletionException && failure.getCause() != null ? failure.getCause() : failure;
	}

//	hits waiting to be sent are held in memory
	public void submit(long guildId, long channelId, String entry) {
		String rejectionReason = SnitchBatchPacker.rejectionReason(entry);
		if (rejectionReason != null) {
			logger.error("Not relaying a snitch hit to channel " + channelId + ": " + rejectionReason);
			return;
		}
		synchronized (lock) {
			if (!running) {
				return;
			}
			Buffer buffer = buffers.computeIfAbsent(channelId, id -> new Buffer(guildId, id));
			buffer.entries.add(new Entry(entry, clock.getAsLong()));
			buffer.contentLength += entry.length();
			pump(buffer);
		}
	}

	// future work: on shutdown, in-flight hits get discarded (same as before batch re-work). implement durable queue
	// of some variety
	public void shutdown() {
		int waiting = 0;
		synchronized (lock) {
			running = false;
			for (Buffer buffer : buffers.values()) {
				waiting += buffer.entries.size();
			}
			buffers.clear();
		}
		if (waiting > 0) {
			logger.warn("Dropping " + waiting + " snitch hits that were still waiting to be sent");
		}
		scheduler.shutdown();
	}

	private void pump(Buffer buffer) {
		if (buffer.sending || buffer.entries.isEmpty()) {
			return;
		}
		long wait = buffer.entries.peek().receivedAt() + COLLECT_WINDOW_MILLIS - clock.getAsLong();
		if (wait <= 0 || buffer.length() > SnitchBatchPacker.TARGET_LENGTH) {
			send(buffer);
			return;
		}
		if (!buffer.flushScheduled) {
			buffer.flushScheduled = true;
			schedule(() -> flush(buffer), wait);
		}
	}

	private void flush(Buffer buffer) {
		synchronized (lock) {
			buffer.flushScheduled = false;
			if (running) {
				pump(buffer);
			}
		}
	}

	private void send(Buffer buffer) {
		List<String> waiting = new ArrayList<>(buffer.entries.size());
		for (Entry entry : buffer.entries) {
			waiting.add(entry.text());
		}
		int count = SnitchBatchPacker.countLeadingEntries(waiting);
		List<String> batch = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			Entry entry = buffer.entries.poll();
			buffer.contentLength -= entry.text().length();
			batch.add(entry.text());
		}
		buffer.sending = true;
		attempt(buffer, SnitchBatchPacker.join(batch), "k" + Long.toUnsignedString(random.nextLong(), 36), 1,
				batch.size());
	}

	private void attempt(Buffer buffer, String content, String nonce, int attempt, int entries) {
		CompletableFuture<Void> result;
		try {
			result = sender.send(buffer.guildId, buffer.channelId, content, nonce);
		} catch (RuntimeException e) {
			result = CompletableFuture.failedFuture(e);
		}
		result.whenComplete((ignored, failure) -> schedule(() -> {
			if (failure != null && attempt < MAX_ATTEMPTS) {
				// the buffer stays busy, so later hits keep their order behind this message
				attempt(buffer, content, nonce, attempt + 1, entries);
				return;
			}
			if (failure != null) {
				logger.error("Could not relay " + entries + " snitch hits to channel " + buffer.channelId + " after "
						+ attempt + " attempts", unwrap(failure));
			}
			synchronized (lock) {
				buffer.sending = false;
				if (running) {
					pump(buffer);
				}
			}
		}, failure != null ? RETRY_DELAY_MILLIS * attempt : 0));
	}

	private void schedule(Runnable task, long delayMillis) {
		try {
			scheduler.schedule(task, delayMillis);
		} catch (RejectedExecutionException e) {
			// shutting down
		}
	}

	public interface Scheduler {

		void schedule(Runnable task, long delayMillis);

		default void shutdown() {
		}
	}

	private record Entry(String text, long receivedAt) {
	}

	private static final class Buffer {

		final long guildId;
		final long channelId;
		final ArrayDeque<Entry> entries = new ArrayDeque<>();
		int contentLength;
		boolean sending;
		boolean flushScheduled;

		Buffer(long guildId, long channelId) {
			this.guildId = guildId;
			this.channelId = channelId;
		}

		int length() {
			return contentLength + Math.max(0, entries.size() - 1) * SnitchBatchPacker.SEPARATOR.length();
		}
	}

	// single daemon thread
	private static final class ExecutorScheduler implements Scheduler {

		private final ScheduledThreadPoolExecutor executor;
		private final Logger logger;

		ExecutorScheduler(Logger logger) {
			AtomicInteger counter = new AtomicInteger();
			this.executor = new ScheduledThreadPoolExecutor(1, runnable -> {
				Thread thread = new Thread(runnable, "snitch-batcher-" + counter.incrementAndGet());
				thread.setDaemon(true);
				return thread;
			});
			this.executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
			this.logger = logger;
		}

		@Override
		public void schedule(Runnable task, long delayMillis) {
			executor.schedule(() -> {
				try {
					task.run();
				} catch (Throwable t) {
					logger.error("Unexpected error while relaying snitch hits", t);
				}
			}, Math.max(0, delayMillis), TimeUnit.MILLISECONDS);
		}

		@Override
		public void shutdown() {
			executor.shutdown();
		}
	}
}
