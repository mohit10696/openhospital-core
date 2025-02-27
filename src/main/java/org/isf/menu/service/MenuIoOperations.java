/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2024 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
 *
 * Open Hospital is a free and open source software for healthcare data management.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * https://www.gnu.org/licenses/gpl-3.0-standalone.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.isf.menu.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.model.GroupMenu;
import org.isf.menu.model.User;
import org.isf.menu.model.UserGroup;
import org.isf.menu.model.UserMenuItem;
import org.isf.permissions.model.GroupPermission;
import org.isf.permissions.model.Permission;
import org.isf.permissions.service.GroupPermissionIoOperationRepository;
import org.isf.utils.db.TranslateOHServiceException;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.model.OHExceptionMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = OHServiceException.class)
@TranslateOHServiceException
public class MenuIoOperations {

	private final UserIoOperationRepository repository;

	private final UserGroupIoOperationRepository groupRepository;

	private final UserMenuItemIoOperationRepository menuRepository;

	private final GroupMenuIoOperationRepository groupMenuRepository;

	private final GroupPermissionIoOperationRepository groupPermissionIoOperationRepository;

	public MenuIoOperations(
		UserIoOperationRepository userIoOperationRepository,
		UserGroupIoOperationRepository userGroupIoOperationRepository,
		UserMenuItemIoOperationRepository userMenuItemIoOperationRepository,
		GroupMenuIoOperationRepository groupMenuIoOperationRepository,
		GroupPermissionIoOperationRepository groupPermissionIoOperationRepository
	) {
		this.repository = userIoOperationRepository;
		this.groupRepository = userGroupIoOperationRepository;
		this.menuRepository = userMenuItemIoOperationRepository;
		this.groupMenuRepository = groupMenuIoOperationRepository;
		this.groupPermissionIoOperationRepository = groupPermissionIoOperationRepository;
	}

	/**
	 * Returns the list of {@link User}s
	 * @return the list of {@link User}s
	 * @throws OHServiceException When error occurs
	 */
	public List<User> getUser() throws OHServiceException {
		return repository.findAllByOrderByUserNameAsc();
	}

	/**
	 * Count all active {@link User}s
	 * @return The number of active users
	 */
	public long countAllActiveUsers() {
		return repository.countAllActiveUsersByDeleted(false);
	}

	/**
	 * Count all active {@link UserGroup}s
	 * @return The number of active groups
	 */
	public long countAllActiveGroups() {
		return repository.countAllActiveGroupsByDeleted(false);
	}

	/**
	 * Returns the list of {@link User}s in specified groupID
	 * @param groupID - the group ID
	 * @return the list of {@link User}s
	 * @throws OHServiceException When error occurs
	 */
	public List<User> getUser(String groupID) throws OHServiceException {
		return repository.findAllByUserGroupIdOrderByUserNameAsc(groupID);
	}

	/**
	 * Returns {@link User} from its username
	 * @param userName - the {@link User}'s username
	 * @return {@link User}
	 * @throws OHServiceException When error occurs
	 */
	public User getUserByName(String userName) throws OHServiceException {
		return repository.findByUserNameAndDeleted(userName, false);
	}

	/**
	 * Returns {@link User} from its username
	 * @param username - the {@link User}'s username
	 * @param withSoftDeletion - Included soft deleted if set to true
	 * @return {@link User}
	 * @throws OHServiceException When error occurs
	 */
	public User getUserByName(String username, boolean withSoftDeletion) throws OHServiceException {
		return withSoftDeletion ? repository.findByUserName(username) : getUserByName(username);
	}

	/**
	 * Returns {@link User} from its username
	 * @param userName - the {@link User}'s username
	 * @return {@link User}
	 * @throws OHServiceException When error occurs
	 */
	public User getUserByNameAndIsDeleted(String userName) throws OHServiceException {
		return repository.findByUserNameAndDeleted(userName, true);
	}

	/**
	 * Returns {@link User} description from its username
	 * @param userName - the {@link User}'s username
	 * @return the {@link User}'s description
	 * @throws OHServiceException When error occurs
	 */
	public String getUsrInfo(String userName) throws OHServiceException {
		User user = repository.findByUserNameAndDeleted(userName, false);
		if (user == null) {
			throw new OHServiceException(new OHExceptionMessage("User not found."));
		}
		return user.getDesc();
	}

	/**
	 * Returns the list of {@link UserGroup}s
	 * @return the list of {@link UserGroup}s
	 * @throws OHServiceException When error occurs
	 */
	public List<UserGroup> getUserGroup() throws OHServiceException {
		return groupRepository.findAllByOrderByCodeAsc();
	}

	/**
	 * Find user group by code
	 * @param groupCode UserGroup code
	 * @return The corresponding {@link UserGroup} if found, {@code null} otherwise
	 */
	public UserGroup findByCode(String groupCode) {
		return groupRepository.findByCodeAndDeleted(groupCode, false);
	}

	/**
	 * Find user group by code
	 * @param groupCode UserGroup code
	 * @param withThrashed Include soft deleted if set to true
	 * @return The corresponding {@link UserGroup} if found, {@code null} otherwise
	 */
	public UserGroup findByCode(String groupCode, boolean withThrashed) {
		return withThrashed ? groupRepository.findByCode(groupCode) : findByCode(groupCode);
	}

	/**
	 * Find user group by code
	 * @param groupCode UserGroup code
	 * @return The corresponding {@link UserGroup} if found, {@code null} otherwise
	 */
	public UserGroup findByCodeAndIsDeleted(String groupCode) {
		return groupRepository.findByCodeAndDeleted(groupCode, true);
	}

	/**
	 * Checks if the specified {@link User} code is already present.
	 * @param userName - the {@link User} code to check.
	 * @return {@code true} if the medical code is already stored, {@code false} otherwise.
	 * @throws OHServiceException if an error occurs during the check.
	 */
	public boolean isUserNamePresent(String userName) throws OHServiceException {
		return repository.existsById(userName);
	}

	/**
	 * Checks if the specified {@link UserGroup} code is already present.
	 * @param groupName - the {@link UserGroup} code to check.
	 * @return {@code true} if the medical code is already stored, {@code false} otherwise.
	 * @throws OHServiceException if an error occurs during the check.
	 */
	public boolean isGroupNamePresent(String groupName) throws OHServiceException {
		return groupRepository.existsById(groupName);
	}

	/**
	 * Inserts a new {@link User} in the DB
	 * @param user - the {@link User} to insert
	 * @return the new {@link User} added to the DB
	 * @throws OHServiceException When failed to create user
	 */
	public User newUser(User user) throws OHServiceException {
		return repository.save(user);
	}

	/**
	 * Updates an existing {@link User} in the DB
	 * @param user - the {@link User} to update
	 * @return the updated {@link User}
	 * @throws OHServiceException When failed to update user
	 */
	public User updateUser(User user) throws OHServiceException {
		repository.updateUser(user.getDesc(), user.getUserGroupName(), user.isDeleted(), user.getUserName());

		return getUserByName(user.getUserName(), true);
	}

	/**
	 * Updates the password of an existing {@link User} in the DB
	 * @param user - the {@link User} to update
	 * @return the {@link User} that has been deleted
	 * @throws OHServiceException When failed to update the password
	 */
	public User updatePassword(User user) throws OHServiceException {
		ensureUserNotDeleted(user.getUserName());
		repository.updatePassword(user.getPasswd(), user.getUserName());
		return getUserByName(user.getUserName());
	}

	/**
	 * Deletes an existing {@link User}
	 * @param user - the {@link User} to delete
	 * @throws OHServiceException When failed to delete user
	 */
	public void deleteUser(User user) throws OHServiceException {
		ensureUserNotDeleted(user.getUserName());
		repository.delete(user);
	}

	public void updateFailedAttempts(String userName, int newFailAttempts) {
		repository.updateFailedAttempts(newFailAttempts, userName);
	}

	public void updateUserLocked(String userName, boolean isLocked, LocalDateTime time) {
		repository.updateUserLocked(isLocked, time, userName);
	}

	public void setLastLogin(String userName, LocalDateTime now) {
		repository.setLastLogin(now, userName);
	}

	/**
	 * Returns the list of {@link UserMenuItem}s that compose the menu for specified {@link User}
	 * @param aUser - the {@link User}
	 * @return the list of {@link UserMenuItem}s
	 * @throws OHServiceException When error occurs
	 */
	public List<UserMenuItem> getMenu(User aUser) throws OHServiceException {
		List<Object[]> menuList = menuRepository.findAllWhereUserId(aUser.getUserName());
		List<UserMenuItem> menu = new ArrayList<>();
		for (Object[] object : menuList) {
			UserMenuItem umi = new UserMenuItem();
			umi.setCode((String) object[0]);
			umi.setButtonLabel((String) object[1]);
			umi.setAltLabel((String) object[2]);
			umi.setTooltip((String) object[3]);
			umi.setShortcut((Character) object[4]);
			umi.setMySubmenu((String) object[5]);
			umi.setMyClass((String) object[6]);
			umi.setASubMenu((Boolean) object[7]);
			umi.setPosition((Integer) object[8]);
			umi.setActive((Integer) object[9] == 1);
			menu.add(umi);
		}
		return menu;
	}

	/**
	 * Returns the list of {@link UserMenuItem}s that compose the menu for specified {@link UserGroup}
	 * @param aGroup - the {@link UserGroup}
	 * @return the list of {@link UserMenuItem}s
	 * @throws OHServiceException When failed to get group menu
	 */
	public List<UserMenuItem> getGroupMenu(UserGroup aGroup) throws OHServiceException {
		List<Object[]> menuList = menuRepository.findAllWhereGroupId(aGroup.getCode());
		List<UserMenuItem> menu = new ArrayList<>();
		for (Object[] object : menuList) {
			boolean active = (Integer) object[9] == 1;
			UserMenuItem umi = new UserMenuItem();
			umi.setCode((String) object[0]);
			umi.setButtonLabel((String) object[1]);
			umi.setAltLabel((String) object[2]);
			umi.setTooltip((String) object[3]);
			umi.setShortcut((Character) object[4]);
			umi.setMySubmenu((String) object[5]);
			umi.setMyClass((String) object[6]);
			umi.setASubMenu((Boolean) object[7]);
			umi.setPosition((Integer) object[8]);
			umi.setActive(active);
			menu.add(umi);
		}
		return menu;
	}

	/**
	 * Replaces the {@link UserGroup} rights
	 * @param aGroup - the {@link UserGroup}
	 * @param menu - the list of {@link UserMenuItem}s
	 * @return {@code true}
	 * @throws OHServiceException When failed to set group menu
	 */
	public boolean setGroupMenu(UserGroup aGroup, List<UserMenuItem> menu) throws OHServiceException {
		deleteGroupMenu(aGroup);
		for (UserMenuItem item : menu) {
			insertGroupMenu(aGroup, item);
		}
		return true;
	}

	private void deleteGroupMenu(UserGroup aGroup) throws OHServiceException {
		groupMenuRepository.deleteWhereUserGroup(aGroup.getCode());
	}

	private GroupMenu insertGroupMenu(UserGroup aGroup, UserMenuItem item) throws OHServiceException {
		GroupMenu groupMenu = new GroupMenu();
		groupMenu.setUserGroup(aGroup.getCode());
		groupMenu.setMenuItem(item.getCode());
		groupMenu.setActive(item.isActive() ? 1 : 0);
		return groupMenuRepository.save(groupMenu);
	}

	/**
	 * Deletes a {@link UserGroup}
	 * @param aGroup - the {@link UserGroup} to delete
	 * @throws OHServiceException When failed to delete group
	 */
	public void deleteGroup(UserGroup aGroup) throws OHServiceException {
		ensureUserGroupNotDeleted(aGroup.getCode());
		groupRepository.delete(aGroup);
	}

	/**
	 * Insert a new {@link UserGroup} with a minimum set of rights
	 * @param aGroup - the {@link UserGroup} to insert
	 * @return the new {@link UserGroup}
	 * @throws OHServiceException When failed to create group
	 */
	public UserGroup newUserGroup(UserGroup aGroup) throws OHServiceException {
		return groupRepository.save(aGroup);
	}

	/**
	 * Insert a new {@link UserGroup} with a minimum set of rights
	 * @param userGroup - the {@link UserGroup} to insert
	 * @param permissions - list of permissions to assign to the group
	 * @return the new {@link UserGroup}
	 * @throws OHServiceException When failed to create user group
	 */
	public UserGroup newUserGroup(UserGroup userGroup, List<Permission> permissions) throws OHServiceException {
		UserGroup newUserGroup = groupRepository.save(userGroup);

		if (permissions != null && permissions.size() > 0) {
			List<GroupPermission> groupPermissions = permissions.stream().map(permission -> {
				GroupPermission groupPermission = new GroupPermission();
				groupPermission.setPermission(permission);
				groupPermission.setUserGroup(newUserGroup);

				return groupPermission;
			}).toList();

			groupPermissionIoOperationRepository.saveAll(groupPermissions);
		}

		return newUserGroup;
	}

	/**
	 * Updates an existing {@link UserGroup} in the DB
	 * @param aGroup - the {@link UserGroup} to update
	 * @return the {@link UserGroup} that has been updated
	 * @throws OHServiceException When failed to update the user group
	 */
	public UserGroup updateUserGroup(UserGroup aGroup) throws OHServiceException {
		groupRepository.update(aGroup.getDesc(), aGroup.isDeleted(), aGroup.getCode());

		return findByCode(aGroup.getCode(), true);
	}

	/**
	 * Updates an existing {@link UserGroup} and the related permissions If permissions list is empty, the existing permissions are kept, otherwise they're
	 * replaced with the provided ones.
	 * @param userGroup - the {@link UserGroup} to update
	 * @return the {@link UserGroup} that has been updated
	 * @throws OHServiceException When failed to update user group
	 */
	public UserGroup updateUserGroup(UserGroup userGroup, List<Permission> permissions) throws OHServiceException {
		UserGroup group = findByCode(userGroup.getCode(), true);
		if (group.isDeleted() && userGroup.isDeleted()) {
			throw new OHServiceException(new OHExceptionMessage(MessageBundle.getMessage("angal.groupsbrowser.alreadysoftdeleted.msg")));
		}
		boolean updated = groupRepository.update(userGroup.getDesc(), userGroup.isDeleted(), userGroup.getCode()) > 0;

		if (updated && permissions != null && !permissions.isEmpty()) {
			groupPermissionIoOperationRepository.deleteAllByUserGroup_Code(userGroup.getCode());
			UserGroup updatedUserGroup = groupRepository.getReferenceById(userGroup.getCode());

			List<GroupPermission> groupPermissions = permissions.stream().map(permission -> {
				GroupPermission groupPermission = new GroupPermission();
				groupPermission.setPermission(permission);
				groupPermission.setUserGroup(updatedUserGroup);

				return groupPermission;
			}).toList();

			groupPermissionIoOperationRepository.saveAll(groupPermissions);
		}

		return findByCode(userGroup.getCode(), true);
	}

	public void ensureUserNotDeleted(String username) throws OHServiceException {
		User entity = repository.findByUserNameAndDeleted(username, true);
		if (entity != null) {
			throw new OHServiceException(new OHExceptionMessage(MessageBundle.getMessage("angal.userbrowser.alreadysoftdeleted.msg")));
		}
	}

	public void ensureUserGroupNotDeleted(String code) throws OHServiceException {
		UserGroup entity = groupRepository.findByCodeAndDeleted(code, true);
		if (entity != null) {
			throw new OHServiceException(new OHExceptionMessage(MessageBundle.getMessage("angal.groupsbrowser.alreadysoftdeleted.msg")));
		}
	}
}
