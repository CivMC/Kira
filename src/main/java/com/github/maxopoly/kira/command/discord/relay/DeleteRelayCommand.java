package com.github.maxopoly.kira.command.discord.relay;

import net.civmc.kira.Kira;
import com.github.maxopoly.kira.command.model.discord.ArgumentBasedCommand;
import com.github.maxopoly.kira.command.model.top.InputSupplier;
import com.github.maxopoly.kira.rabbit.session.PermissionCheckSession;
import com.github.maxopoly.kira.relay.GroupChat;
import com.github.maxopoly.kira.relay.GroupChatManager;
import com.github.maxopoly.kira.user.KiraUser;

public class DeleteRelayCommand extends ArgumentBasedCommand {

	public DeleteRelayCommand() {
		super("deleterelay", 1);
		setRequireIngameAccount();
	}

	@Override
	public String getFunctionality() {
		return "Deletes a relay";
	}

	@Override
	public String getRequiredPermission() {
		return "isauth";
	}

	@Override
	public String getUsage() {
		return "deleterelay [group]";
	}

	@Override
	public String handle(InputSupplier sender, String[] args) {
		KiraUser user = sender.getUser();
		GroupChatManager man = Kira.Companion.getInstance().getGroupChatManager();
		GroupChat chat = man.getGroupChat(args[0]);
		if (chat == null) {
			return "No group chat with the name " + args[0] + " is known";
		}
		Kira.Companion.getInstance().getRequestSessionManager().request(new PermissionCheckSession(user.getIngameUUID(),
				chat.getName(), GroupChatManager.getNameLayerManageChannelPermission()) {

			@Override
			public void handlePermissionReply(boolean hasPerm) {
				if (!hasPerm && !sender.hasPermission("admin")) {
					sender.reportBack("You do not have permission to delete this relay");
					return;
				}

				GroupChat chat = man.getGroupChat(args[0]);
				if (chat == null) {
					logger.warn("Failed to delete group chat"+ args[0] + ", it was already gone");
					sender.reportBack("Channel deletion failed, channel was already gone");
					return;
				}
				logger.info("Attempting to delete group of chat for " + chat.getName() + " as initiated by " + user.toString());
				Kira.Companion.getInstance().getGroupChatManager().deleteGroupChat(chat);
				sender.reportBack("Successfully removed relay for group " + chat.getName());
			}
		});
		return "Requesting permission confirmation from server...";
	}

}
