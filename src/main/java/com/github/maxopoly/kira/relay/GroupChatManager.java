package com.github.maxopoly.kira.relay;

import net.civmc.kira.Kira;
import com.github.maxopoly.kira.database.DAO;
import com.github.maxopoly.kira.permission.KiraRole;
import com.github.maxopoly.kira.permission.KiraRoleManager;
import com.github.maxopoly.kira.user.KiraUser;
import com.github.maxopoly.kira.user.UserManager;
import net.dv8tion.jda.api.entities.*;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class GroupChatManager {

	private static final String ACCESS_PERM_SUFFIX = "_ACCESS";
	private static final long CHANNEL_PERMS = 379968L;

	public static float getChatCountLimit() {
		return 4.0f;
	}

	public static String getNameLayerManageChannelPermission() {
		return "KIRA_MANAGE_CHANNEL";
	}

	private final Logger logger;
	private final DAO databaseManager;
	private final long sectionID;
	private final RelayConfigManager relayConfigManager;

	private final Map<String, GroupChat> groupChatByName;
	private final Map<Long, Set<GroupChat>> chatsByChannelId;
	private final Map<Integer, Float> ownedChatsByUserId;

	public GroupChatManager(final Logger logger,
							final DAO databaseManager,
							final long sectionID,
							final RelayConfigManager relayConfigManager) {
		this.logger = logger;
		this.databaseManager = databaseManager;
		this.sectionID = sectionID;
		this.relayConfigManager = relayConfigManager;

		this.groupChatByName = new ConcurrentHashMap<>();
		this.chatsByChannelId = new TreeMap<>();
		this.ownedChatsByUserId = new TreeMap<>();

		for (GroupChat chat : this.databaseManager.loadGroupChats(relayConfigManager)) {
			putGroupChat(chat);
		}
		logger.info("Loaded " + this.groupChatByName.size() + " group chats from database");
	}

	public void addMember(GroupChat chat, KiraUser user) {
		KiraRoleManager roleMan = Kira.Companion.getInstance().getKiraRoleManager();
		if (!roleMan.getRoles(user).contains(chat.getTiedRole())) {
			logger.info("Giving tied role for chat " + chat.getName() + " to " + user.toString());
			Kira.Companion.getInstance().getKiraRoleManager().giveRoleToUser(user, chat.getTiedRole());
		}
		Guild guild = Kira.Companion.getInstance().getGuild();
		if (guild.getIdLong() == chat.getGuildId()) {
			TextChannel channel = Kira.Companion.getInstance().getJda().getTextChannelById(chat.getDiscordChannelId());

			if (channel == null) {
				logger.error(
						"Could not update member perm on channel for group " + chat.getName() + ", it didnt exist");
				return;
			}

			guild.retrieveMemberById(user.getDiscordID()).submit()
					.whenComplete((member, error) -> {
						if (error != null) {
							logger.error("Could not update member perm on channel for member " + user.getDiscordID());
							return;
						}

						PermissionOverride perm = channel.getPermissionOverride(member);
						if (perm == null) {
							perm = channel.createPermissionOverride(member).complete();
						}
						if (perm.getAllowedRaw() != CHANNEL_PERMS) {
							logger.info("Adjusting channel perms to " + chat.getName() + " for " + user.toString());
							perm.getManager().grant(CHANNEL_PERMS).queue();
						}
					});
		}
	}

	public void applyToAll(Consumer<GroupChat> function) {
		for (GroupChat chat : groupChatByName.values()) {
			function.accept(chat);
		}
	}

	public GroupChat createGroupChat(String name, KiraUser creator) {
		Guild guild = Kira.Companion.getInstance().getGuild();
		Category cat = guild.getCategoryById(sectionID);
		if (cat == null) {
			logger.warn("Tried to create channel, but category for it could not be found");
			return null;
		}
		TextChannel channel = cat.createTextChannel(name).complete();
		if (channel == null) {
			logger.warn("Tried to create channel, but it didn't work");
			return null;
		}
		return createGroupChat(name, guild.getIdLong(), channel.getIdLong(), creator);
	}

	public GroupChat createGroupChat(String name, long guildID, long channelID, KiraUser creator) {
		KiraRole role = Kira.Companion.getInstance().getKiraRoleManager().getOrCreateRole(name + ACCESS_PERM_SUFFIX);
		int id = databaseManager.createGroupChat(guildID, channelID, name, role, creator.getID(),
				relayConfigManager.getDefaultConfig());
		if (id == -1) {
			return null;
		}
		GroupChat chat = new GroupChat(id, name, channelID, guildID, role, creator,
				relayConfigManager.getDefaultConfig());
		putGroupChat(chat);
		logger.info("Successfully created group chat for group " + chat.toString());
		return chat;
	}

	public void deleteGroupChat(GroupChat chat) {
		TextChannel channel = Kira.Companion.getInstance().getJda().getTextChannelById(chat.getDiscordChannelId());
		boolean isManaged;
		if (channel == null) {
			// already deleted
			isManaged = false;
		} else {
			Category category = channel.getParent();
			boolean isInMainGuild = channel.getGuild().getIdLong() == Kira.Companion.getInstance().getGuild().getIdLong();
			boolean isInRelaySection = category != null && category.getIdLong() == Kira.Companion.getInstance().getConfig().getRelaySectionID();

			isManaged = isInMainGuild && isInRelaySection;
		}
		logger.info("Deleting channel for " + chat.getName());
		if (isManaged) {
			channel.delete().queue();
		}
		Float count = ownedChatsByUserId.get(chat.getCreator().getID());
		if (count != null) {
			ownedChatsByUserId.put(chat.getCreator().getID(), Math.max(0, count - 1));
		}
		groupChatByName.remove(chat.getName().toLowerCase());
		Set<GroupChat> channels = chatsByChannelId.get(chat.getDiscordChannelId());
		if (channels != null) {
			channels.remove(chat);
		}
		// db clean up is done by deleting the chat via foreign keys
		Kira.Companion.getInstance().getKiraRoleManager().deleteRole(chat.getTiedRole(), false);
		databaseManager.deleteGroupChat(chat);
		if (!isManaged && channel != null) {
			channel.sendMessage("Relay " + chat.getName()
					+ " which was previously linked to this channel is being deleted as requested by a user. It will no longer broadcast anything")
					.queue();
		}
	}

	public Set<GroupChat> getChatByChannelID(long id) {
		Set<GroupChat> existing = chatsByChannelId.get(id);
		if (existing == null) {
			return new TreeSet<>();
		}
		return existing;
	}

	public GroupChat getGroupChat(String name) {
		return groupChatByName.get(name.toLowerCase());
	}

	public float getOwnedChatCount(KiraUser user) {
		Float count = ownedChatsByUserId.get(user.getID());
		if (count == null) {
			count = 0.0f;
		}
		return count;
	}

	public void putGroupChat(GroupChat chat) {
		groupChatByName.put(chat.getName().toLowerCase(), chat);
		Set<GroupChat> existing = chatsByChannelId.get(chat.getDiscordChannelId());
		if (existing == null) {
			existing = new HashSet<>();
			chatsByChannelId.put(chat.getDiscordChannelId(), existing);
		}
		existing.add(chat);
		Float count = ownedChatsByUserId.get(chat.getCreator().getID());
		if (count == null) {
			count = 0.0f;
		}
		ownedChatsByUserId.put(chat.getCreator().getID(), count + chat.getWeight());
	}

	public void removeMember(GroupChat chat, KiraUser user) {
		Kira.Companion.getInstance().getKiraRoleManager().takeRoleFromUser(user, chat.getTiedRole());
		logger.info("Taking tied role for chat " + chat.getName() + " from " + user.toString());
		// TODO
	}

	public void setConfig(GroupChat chat, RelayConfig config) {
		databaseManager.setRelayConfigForChat(chat, config);
		chat.setConfig(config);
	}

	public void syncAccess(GroupChat chat, Set<Integer> intendedMembers) {
		UserManager userMan = Kira.Companion.getInstance().getUserManager();
		Set<Integer> currentMembers = databaseManager.getGroupChatMembers(chat);
		// remove all members that shouldnt be there
		currentMembers.removeIf(Predicate.not(intendedMembers::contains));
		currentMembers.forEach(member -> removeMember(chat, userMan.getUser(member)));
		// add all that are missing
		intendedMembers.forEach(i -> addMember(chat, userMan.getUser(i)));
	}

	public List<GroupChat> getGroupChats() {
		return new ArrayList<>(this.groupChatByName.values());
	}

}
