package com.github.maxopoly.kira.command.discord.relay;

import com.github.maxopoly.kira.KiraMain;
import com.github.maxopoly.kira.relay.GroupId;
import com.github.maxopoly.kira.command.model.discord.ArgumentBasedCommand;
import com.github.maxopoly.kira.command.model.top.InputSupplier;
import com.github.maxopoly.kira.rabbit.session.PermissionCheckSession;
import com.github.maxopoly.kira.relay.GroupChat;
import com.github.maxopoly.kira.relay.GroupChatManager;
import com.github.maxopoly.kira.relay.RelayConfig;
import com.github.maxopoly.kira.user.KiraUser;

public class TieRelayConfigCommand extends ArgumentBasedCommand {

	public TieRelayConfigCommand() {
		super("setrelayconfig", 2, 3);
		setRequireIngameAccount();
	}

	@Override
	public String getFunctionality() {
		return "Sets which configuration to use for a specific relay";
	}

	@Override
	public String getRequiredPermission() {
		return "isauth";
	}

	@Override
	public String getUsage() {
		return "setrelayconfig [server (optional] [group] [relay]";
	}

	@Override
	public String handle(InputSupplier sender, String[] args) {
		KiraUser user = sender.getUser();
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
        if (serverSelected && args.length < 3) {
            return "You provided a server name but not a group and relay!";
        }
		GroupChat chat = KiraMain.getInstance().getGroupChatManager().getGroupChat(new GroupId(server, args[0].toLowerCase()));
		if (chat == null) {
			return "No group chat with the name " + args[0] + " is known";
		}
		RelayConfig config = KiraMain.getInstance().getRelayConfigManager().getByName(args[1]);
		if (config == null) {
			return "No relay config with the name " + args[0] + " is known";
		}
		KiraMain.getInstance().getRequestSessionManager().request(server, new PermissionCheckSession(user.getIngameUUID(),
				chat.getName(), GroupChatManager.getNameLayerManageChannelPermission()) {

			@Override
			public void handlePermissionReply(boolean hasPerm) {
				if (!hasPerm && !sender.hasPermission("admin")) {
					sender.reportBack("You do not have permission to set the config for this relay");
					return;
				}

				KiraMain.getInstance().getGroupChatManager().setConfig(chat, config);
				sender.reportBack("Successfully set relay config for " + chat.getName() + " to " + config.getName());
			}
		});
		return "Requesting permission confirmation from server...";
	}

}
