package com.github.maxopoly.kira.relay.snitch;

import java.util.List;


// Decides which formatted snitch entries are combined into one Discord message
public final class SnitchBatchPacker {

	/** A message holding more than one entry stays at or below this length. */
	public static final int TARGET_LENGTH = 1900;
	/** Discord's limit for message content. */
	public static final int MAX_LENGTH = 2000;
	public static final String SEPARATOR = "\n";

	private SnitchBatchPacker() {
	}

	 // Counts how many entries from the start of the list belong in the next message
	public static int countLeadingEntries(List<String> entries) {
		if (entries.isEmpty()) {
			return 0;
		}
		int length = entries.get(0).length();
		if (length > TARGET_LENGTH) {
			return 1;
		}
		int count = 1;
		while (count < entries.size()) {
			int next = length + SEPARATOR.length() + entries.get(count).length();
			if (next > TARGET_LENGTH) {
				break;
			}
			length = next;
			count++;
		}
		return count;
	}

	public static String join(List<String> entries) {
		return String.join(SEPARATOR, entries);
	}

	/**
	 * @return why Discord would reject this entry, or null if it can be sent
	 */
	public static String rejectionReason(String entry) {
		if (entry.isBlank()) {
			return "formatted entry is empty";
		}
		if (entry.length() > MAX_LENGTH) {
			return "formatted entry is " + entry.length() + " characters, Discord allows " + MAX_LENGTH;
		}
		return null;
	}
}
