package com.github.maxopoly.kira.user;

import net.civmc.kira.Kira;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class DiscordRoleManager {
	private long authRoleID;
	private Logger logger;
	private UserManager userManager;

	private final ScheduledExecutorService scheduler;

	public DiscordRoleManager(long authRoleID, Logger logger, UserManager userManager) {
		this.userManager = userManager;
		this.authRoleID = authRoleID;
		this.logger = logger;
		this.scheduler = Executors.newScheduledThreadPool(1);
	}

	public void syncUser(KiraUser user) {
		Guild guild = Kira.Companion.getInstance().getGuild();

		guild.retrieveMemberById(user.getDiscordID()).submit()
				.whenComplete((member, error) -> {
					if (error != null) {
						return;
					}

					syncMember(member);
				});
	}

	/**
	 * Give the discord member the auth role if they need it.
	 * Take the auth role from them if they shouldn't have it.
	 */
	public void syncMember(Member member) {
		Guild guild = Kira.Companion.getInstance().getGuild();

		// We don't want to update any roles for the owner of the guild. Trust me.
		if (member.isOwner()) {
			return;
		}

		KiraUser user = userManager.getUserByDiscordID(member.getIdLong());

		// If the user does not exist, remove the auth role
		if (user == null || !user.hasIngameAccount()) {
			takeDiscordRole(guild, member);
		} else {
			// If the user does exist, give the auth role and update their nickname
			giveDiscordRole(guild, user);
			setName(guild, user);
		}
	}

	/**
	 * Sync all of our known users. Really this should just add roles to those who need it.
	 *
	 * TODO: Not used for now due to rate limiting. Maybe schedule this to be during daily reset?
	 */
	public void syncAllUsers() {
		userManager.getAllUsers().forEach(this::syncUser);
	}

	/**
	 * Sync all members in the discord.
	 *
	 * Warning! Do NOT try to use this unless the GUILD_MEMBERS intent is enabled.
	 * This is here for the future for when we do get over that bridge.
	 */
	public void syncAllMembers() {
		Kira.Companion.getInstance().getGuild().loadMembers().
				onSuccess(members -> members.forEach(this::syncMember));
	}

	public void setName(Guild guild, KiraUser user) {
		Member self = guild.getSelfMember();

		guild.retrieveMemberById(user.getDiscordID()).submit()
				.whenComplete((member, error) -> {
					if (error != null ) {
						return;
					}

					if (self.canInteract(member)) {
						guild.modifyNickname(member, user.getName()).queue();
					}
				});
	}

	public CompletableFuture<Boolean> giveDiscordRole(Guild guild, KiraUser user) {
		if (!user.hasDiscord()) {
			logger.warn("Could not add role to " + user + ", no discord account associated");
			return CompletableFuture.completedFuture(false);
		}

		CompletableFuture<Boolean> future = new CompletableFuture<>();
		guild.retrieveMemberById(user.getDiscordID()).submit()
				.whenComplete((member, error) -> {
					if (error != null) {
						logger.warn("Could not give role to " + user + ", discord account not found");
						future.complete(false);
					}

					future.complete(giveDiscordRole(guild, member));
				});

		return future;
	}

	public boolean giveDiscordRole(Guild guild, Member member) {
		Role role = guild.getRoleById(authRoleID);

		if (member == null) {
			logger.warn("Could not give role to null member");
			return false;
		}

		if (role == null) {
			logger.warn("Could not give role to " + member.getEffectiveName() + ", role with id " + authRoleID
					+ "did not exist");
			return false;
		}

		logger.info("Giving auth role to " + member.getEffectiveName());
		guild.addRoleToMember(member, role).queue();
		return true;
	}

	public CompletableFuture<Boolean> takeDiscordRole(Guild guild, KiraUser user) {

		if (!user.hasDiscord()) {
			logger.warn("Could not remove role from " + user + ", no discord account associated");
			return CompletableFuture.completedFuture(false);
		}

		CompletableFuture<Boolean> future = new CompletableFuture<>();
		guild.retrieveMemberById(user.getDiscordID()).submit()
				.whenComplete((member, error) -> {
					if (error != null) {
						logger.warn("Could not remove role from " + user + ", discord account not found");
						future.complete(false);
					}

					future.complete(takeDiscordRole(guild, member));
				});

		return future;
	}

	public boolean takeDiscordRole(Guild guild, Member member) {
		Role role = guild.getRoleById(authRoleID);
		if (member == null) {
			logger.warn("Could not remove null member");
			return false;
		}
		if (role == null) {
			logger.warn("Could not remove role from " + member.getEffectiveName() + ", role with id " + authRoleID
					+ " did not exist");
			return false;
		}
		logger.info("Taking auth role from " + member.getEffectiveName());
		guild.removeRoleFromMember(member, role).queue();
		return true;
	}
}
