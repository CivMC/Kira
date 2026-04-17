package com.github.maxopoly.kira.user;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class AuthManager {

	private static final long CODE_EXPIRATION_MILLIS = 5 * 60 * 1000L;

	private Map<String, PendingAuth> pendingAuths;
	private Map<UUID, String> playerNames;

	public AuthManager() {
		pendingAuths = new HashMap<>();
		playerNames = new HashMap<>();
	}

	public String getName(UUID uuid) {
		return playerNames.get(uuid);
	}

	public UUID getUserForCode(String code) {
		purgeExpired();
		PendingAuth pending = pendingAuths.get(code.toLowerCase());
        return pending == null ? null : pending.uuid;
    }

	public void putCode(UUID user, String name, String auth) {
		purgeExpired();
		pendingAuths.put(auth.toLowerCase(), new PendingAuth(user, System.currentTimeMillis()));
		playerNames.put(user, name);
	}

	public void removeCode(String code) {
		PendingAuth pending = pendingAuths.remove(code.toLowerCase());
		if (pending != null && !hasOtherPendingCode(pending.uuid)) {
			playerNames.remove(pending.uuid);
		}
	}

	private boolean isExpired(PendingAuth pending) {
		return System.currentTimeMillis() - pending.createdAt > CODE_EXPIRATION_MILLIS;
	}

	private boolean hasOtherPendingCode(UUID uuid) {
		for (PendingAuth pending : pendingAuths.values()) {
			if (pending.uuid.equals(uuid)) {
				return true;
			}
		}
		return false;
	}

	private void purgeExpired() {
		Iterator<Map.Entry<String, PendingAuth>> iter = pendingAuths.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, PendingAuth> entry = iter.next();
			if (isExpired(entry.getValue())) {
				iter.remove();
				UUID uuid = entry.getValue().uuid;
				if (!hasOtherPendingCode(uuid)) {
					playerNames.remove(uuid);
				}
			}
		}
	}

    private record PendingAuth(UUID uuid, long createdAt) {
    }

}
