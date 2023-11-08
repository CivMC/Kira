package com.github.maxopoly.kira.permission;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.civmc.kira.Kira;
import com.github.maxopoly.kira.user.KiraUser;

public class KiraRoleManager {

	private Map<Integer, KiraRole> roleById;
	private Map<String, KiraRole> roleByName;
	private Map<Integer, Set<KiraRole>> userRoles;
	private Map<String, KiraPermission> permissionsByName;
	private Map<Integer, KiraPermission> permissionsById;

	public KiraRoleManager() {
		roleById = new TreeMap<>();
		userRoles = new TreeMap<>();
		roleByName = new HashMap<>();
		permissionsByName = new HashMap<>();
		permissionsById = new TreeMap<>();
	}

	public void addPermissionToRole(KiraRole role, KiraPermission perm, boolean writeToDb) {
		if (role.hasPermission(perm)) {
			return;
		}
		role.addPermission(perm);
		if (writeToDb) {
			Kira.Companion.getInstance().getDao().addPermissionToRole(perm, role);
		}
	}

	public void addRole(int userID, KiraRole role, boolean saveToDb) {
		Set<KiraRole> existingRoles = userRoles.computeIfAbsent(userID, s ->  new HashSet<>());
		if (existingRoles.contains(role)) {
			return;
		}
		existingRoles.add(role);
		if (saveToDb) {
			Kira.Companion.getInstance().getDao().addUserToRole(Kira.Companion.getInstance().getUserManager().getUser(userID),
					role);
		}
	}

	public void deleteRole(KiraRole role, boolean writeToDb) {
		roleById.remove(role.getID());
		roleByName.remove(role.getName());
		for(Set<KiraRole> roles : userRoles.values()) {
			roles.remove(role);
		}
		if (writeToDb) {
			Kira.Companion.getInstance().getDao().deleteRole(role);
		}
	}

	public KiraRole getDefaultRole() {
		return getRole("default");
	}

	public KiraPermission getOrCreatePermission(String name) {
		// check cache first
		KiraPermission perm = permissionsByName.get(name);
		if (perm != null) {
			return perm;
		}
		perm = Kira.Companion.getInstance().getDao().retrieveOrCreatePermission(name);
		if (perm != null) {
			registerPermission(perm);
		}
		return perm;
	}

	public KiraRole getOrCreateRole(String name) {
		// check cache first
		KiraRole role = roleByName.get(name);
		if (role != null) {
			return role;
		}
		role = Kira.Companion.getInstance().getDao().retrieveOrCreateRole(name);
		if (role != null) {
			registerRole(role);
		}
		return role;
	}

	public KiraPermission getPermission(int id) {
		return permissionsById.get(id);
	}

	public KiraPermission getPermission(String name) {
		return permissionsByName.get(name);
	}

	public KiraRole getRole(int id) {
		return roleById.get(id);
	}

	public KiraRole getRole(String name) {
		return roleByName.get(name);
	}

	public Collection<KiraRole> getRoles(KiraUser user) {
		Set<KiraRole> roleSet = userRoles.get(user.getID());
		if (roleSet == null) {
			roleSet = new HashSet<>();
		}
		return Collections.unmodifiableCollection(roleSet);
	}

	public void giveRoleToUser(KiraUser user, KiraRole role) {
		addRole(user.getID(), role, true);
	}

	public boolean hasPermission(KiraUser user, String perm) {
		Set<KiraRole> existingRoles = userRoles.get(user.getID());
		if (existingRoles == null) {
			return false;
		}
		for (KiraRole role : existingRoles) {
			if (role.hasPermission(perm)) {
				return true;
			}
		}
		return false;
	}

	public void registerPermission(KiraPermission perm) {
		permissionsByName.put(perm.getName(), perm);
	}

	public void registerRole(KiraRole role) {
		roleById.put(role.getID(), role);
		roleByName.put(role.getName(), role);
	}

	public void reload(KiraRoleManager newData) {
		this.roleById = newData.roleById;
		this.roleByName = newData.roleByName;
		this.userRoles = newData.userRoles;
	}

	public void setupDefaultPermissions() {
		KiraRole defaultRole = getOrCreateRole("default");
		KiraPermission defaultPerm = getOrCreatePermission("default");
		KiraPermission canAuthPerm = getOrCreatePermission("canauth");
		addPermissionToRole(defaultRole, defaultPerm, true);
		addPermissionToRole(defaultRole, canAuthPerm, true);
		KiraRole adminRole = getOrCreateRole("admin");
		KiraPermission adminPerm = getOrCreatePermission("admin");
		addPermissionToRole(adminRole, adminPerm, true);
		KiraRole authRole = getOrCreateRole("auth");
		KiraPermission authPerm = getOrCreatePermission("isauth");
		addPermissionToRole(authRole, authPerm, true);
	}

	public void takeRoleFromUser(KiraUser user, KiraRole role) {
		Set<KiraRole> existingRoles = userRoles.get(user.getID());
		if (existingRoles == null) {
			return;
		}
		existingRoles.remove(role);
		Kira.Companion.getInstance().getDao().takeRoleFromUser(user, role);
	}
}
