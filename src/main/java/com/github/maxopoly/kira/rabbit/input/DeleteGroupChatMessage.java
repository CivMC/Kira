package com.github.maxopoly.kira.rabbit.input;

import com.github.maxopoly.kira.rabbit.RabbitInputSupplier;
import com.github.maxopoly.kira.relay.GroupChat;
import com.github.maxopoly.kira.relay.GroupChatManager;
import com.github.maxopoly.kira.relay.GroupId;
import com.github.maxopoly.kira.user.KiraUser;
import net.civmc.kira.Kira;
import org.json.JSONObject;

import java.util.UUID;

public class DeleteGroupChatMessage extends RabbitMessage {

	public DeleteGroupChatMessage() {
		super("deletegroupchat");
	}

	@Override
	public void handle(JSONObject json, RabbitInputSupplier supplier) {
        String server = json.getString("server");
        if (server == null) {
            return;
        }
        UUID destroyerUUID = UUID.fromString(json.getString("sender"));
        KiraUser destroyer = Kira.Companion.getInstance().getUserManager().getUserByIngameUUID(destroyerUUID);
		if (destroyer == null) {
			Kira.Companion.getInstance().getMcRabbitGateway().sendMessage(server, destroyerUUID, "Channel deletion failed, "
					+ "no discord account tied");
			return;
		}
		String group = json.getString("group");
		GroupChatManager man = Kira.Companion.getInstance().getGroupChatManager();
		GroupChat chat = man.getGroupChat(new GroupId(server, group.toLowerCase()));
		if (chat == null) {
			logger.warn("Failed to delete group chat"+ group + ", it was already gone");
			Kira.Companion.getInstance().getMcRabbitGateway().sendMessage(server, destroyerUUID, "Channel deletion failed, no channel found");
			return;
		}
		logger.info("Attempting delete group of chat for " + group + " as initiated by " + destroyer.toString());
		man.deleteGroupChat(chat);
		Kira.Companion.getInstance().getMcRabbitGateway().sendMessage(server, destroyerUUID, "Deleted channel successfully");
	}

}

