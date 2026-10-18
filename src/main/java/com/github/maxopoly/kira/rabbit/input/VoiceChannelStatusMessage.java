package com.github.maxopoly.kira.rabbit.input;

import com.github.maxopoly.kira.KiraMain;
import com.github.maxopoly.kira.rabbit.RabbitInputSupplier;
import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import org.json.JSONObject;

public final class VoiceChannelStatusMessage extends RabbitMessage {

    // Channel renames have a much tighter Discord rate limit than normal messages.
    private static final long RENAME_INTERVAL = Duration.ofMinutes(5).plusSeconds(1).toNanos();
    private final boolean queue;
    private long nextRename;
    private boolean pending;

    private final Timer timer = new Timer();
    private TimerTask task;

    public VoiceChannelStatusMessage(final boolean queue) {
        super(queue ? "queuestatus" : "serverstatus");
        this.queue = queue;

        scheduleOffline();
    }

    private VoiceChannel getVoiceChannel() {
        final KiraMain kira = KiraMain.getInstance();
        final long channelId = queue ? kira.getConfig().getQueueVoiceChannelId()
            : kira.getConfig().getMainVoiceChannelId();
        if (channelId <= 0) {
            return null;
        }

        return kira.getGuild().getVoiceChannelById(channelId);
    }

    private synchronized void scheduleOffline() {
        VoiceChannel channel = getVoiceChannel();
        if (channel == null) {
            return;
        }
        if (this.task != null) {
            this.task.cancel();
        }
        this.task = new TimerTask() {
            @Override
            public void run() {
                channel.getManager().setName((queue ? "Queue: " : "Main: ") + "OFFLINE").queue(ignored -> {}, error -> scheduleOffline());
            }
        };
        this.timer.schedule(this.task, 120_000);
    }

    @Override
    public synchronized void handle(final JSONObject json, final RabbitInputSupplier supplier) {
        if (!json.optString("server").equals(queue ? "proxy" : "main")
                || (queue && !"main".equals(json.optString("target")))) {
            return;
        }
        // Ignore old snapshots left in RabbitMQ while Kira was offline.
        if (Math.abs(System.currentTimeMillis() - json.getLong("timestamp")) > 60_000) {
            return;
        }
        if (pending || (nextRename != 0 && System.nanoTime() - nextRename < 0)) {
            scheduleOffline();
            return;
        }
        final int players = json.getInt("players");
        final int cap = queue ? 0 : json.getInt("playerCap");
        if (players < 0 || cap < 0) {
            return;
        }
        final String name = queue ? "Queue: " + players : "Main: " + players + "/" + cap;
        final VoiceChannel channel = getVoiceChannel();
        if (channel == null) {
            return;
        }
        if (name.equals(channel.getName())) {
            scheduleOffline();
            return;
        }
        nextRename = System.nanoTime() + RENAME_INTERVAL;
        pending = true;
        try {
            channel.getManager().setName(name).queue(ignored -> {
                finishRename();
                scheduleOffline();
            }, error -> {
                KiraMain.getInstance().getLogger().warn("Failed to rename status voice channel", error);
                finishRename();
                scheduleOffline();
            });

        } catch (final RuntimeException exception) {
            pending = false;
            KiraMain.getInstance().getLogger().warn("Failed to rename status voice channel", exception);
        }
    }

    private synchronized void finishRename() {
        pending = false;
        nextRename = System.nanoTime() + RENAME_INTERVAL;
    }
}
