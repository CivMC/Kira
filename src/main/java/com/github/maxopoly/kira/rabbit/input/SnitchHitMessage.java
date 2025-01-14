package com.github.maxopoly.kira.rabbit.input;

import org.json.JSONObject;

import net.civmc.kira.Kira;
import com.github.maxopoly.kira.rabbit.RabbitInputSupplier;
import com.github.maxopoly.kira.relay.GroupChat;
import com.github.maxopoly.kira.relay.GroupChatManager;
import com.github.maxopoly.kira.relay.actions.MinecraftLocation;
import com.github.maxopoly.kira.relay.actions.PlayerHitSnitchAction;
import com.github.maxopoly.kira.relay.actions.SnitchHitType;
import com.github.maxopoly.kira.relay.actions.SnitchType;

public class SnitchHitMessage extends RabbitMessage {

	public SnitchHitMessage() {
		super("sendsnitchhit");
	}

	@Override
	public void handle(JSONObject json, RabbitInputSupplier supplier) {
		String groupName = json.getString("groupName");
		GroupChatManager man = Kira.Companion.getInstance().getGroupChatManager();
		GroupChat chat = man.getGroupChat(groupName);
		if (chat == null || !chat.getConfig().shouldShowSnitches()) {
			return;
		}
		String snitchName = json.getString("snitchName");
		// UUID victimUUID = UUID.fromString(json.getString("victimUUID"));
		String victimName = json.getString("victimName");
		int x = json.getInt("x");
		int y = json.getInt("y");
		int z = json.getInt("z");
		String world = json.optString("world", "world");
		SnitchHitType hitType = SnitchHitType.valueOf(json.optString("type", "ENTER"));
		SnitchType snitchType = SnitchType.getType(json.optString("snitchtype", "ENTRY"));
		long timestamp = json.optLong("timestamp", System.currentTimeMillis());
		PlayerHitSnitchAction snitchAction = new PlayerHitSnitchAction(timestamp, victimName, snitchName, groupName,
				new MinecraftLocation(world, x, y, z), hitType, snitchType);
		Kira.Companion.getInstance().getApiSessionManager().handleSnitchHit(snitchAction);
		if (!chat.sendSnitchHit(snitchAction)) {
			Kira.Companion.getInstance().getLogger()
					.info("Failed to send snitch hit to group " + groupName + ". Channel did not exist");
		}
	}
}
