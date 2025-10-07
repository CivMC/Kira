package com.github.maxopoly.kira.command.discord.relay;

import com.github.maxopoly.kira.KiraMain;
import com.github.maxopoly.kira.command.model.discord.ArgumentBasedCommand;
import com.github.maxopoly.kira.command.model.top.InputSupplier;
import com.github.maxopoly.kira.rabbit.session.PermissionCheckSession;
import com.github.maxopoly.kira.relay.GroupChat;
import com.github.maxopoly.kira.relay.GroupChatManager;
import com.github.maxopoly.kira.relay.GroupId;
import com.github.maxopoly.kira.user.KiraUser;

public class DeleteRelayCommand extends ArgumentBasedCommand {

	public DeleteRelayCommand() {
		super("deleterelay", 1, 2);
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
		return "deleterelay [server (optional)] [group]";
	}

	@Override
	public String handle(InputSupplier sender, String[] args) {
		KiraUser user = sender.getUser();
		GroupChatManager man = KiraMain.getInstance().getGroupChatManager();
        String[] servers = KiraMain.getInstance().getConfig().getServers();
        String server = servers[0];
        boolean serverSelected = false;
        for (String configServer : servers) {
            if (args[0].equalsIgnoreCase(configServer)) {
                server = configServer;
                serverSelected = true;
                break;
            }
        }
        if (serverSelected && args.length < 2) {
            return "You provided a server name but no group name!";
        }
        String group = args[serverSelected ? 1 : 0];
		GroupChat chat = man.getGroupChat(new GroupId(server, group.toLowerCase()));
		if (chat == null) {
			return "No group chat with the name " + group + " on server `" + server + "` is known";
		}
        String fserver = server;
		KiraMain.getInstance().getRequestSessionManager().request(server, new PermissionCheckSession(user.getIngameUUID(),
				chat.getName(), GroupChatManager.getNameLayerManageChannelPermission()) {

			@Override
			public void handlePermissionReply(boolean hasPerm) {
				if (!hasPerm && !sender.hasPermission("admin")) {
					sender.reportBack("You do not have permission to delete this relay");
					return;
				}

				GroupChat chat = man.getGroupChat(new GroupId(fserver, group.toLowerCase()));
				if (chat == null) {
					logger.warn("Failed to delete group chat"+ group + ", it was already gone");
					sender.reportBack("Channel deletion failed, channel was already gone");
					return;
				}
				logger.info("Attempting to delete group of chat for " + chat.getName() + " as initiated by " + user.toString());
				KiraMain.getInstance().getGroupChatManager().deleteGroupChat(chat);
				sender.reportBack("Successfully removed relay for group " + chat.getName());
			}
		});
		return "Requesting permission confirmation from server...";
	}

}
