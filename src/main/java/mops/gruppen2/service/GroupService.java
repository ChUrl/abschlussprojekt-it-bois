package mops.gruppen2.service;

import mops.gruppen2.domain.Account;
import mops.gruppen2.domain.Group;
import mops.gruppen2.domain.GroupType;
import mops.gruppen2.domain.Role;
import mops.gruppen2.domain.User;
import mops.gruppen2.domain.Visibility;
import mops.gruppen2.domain.event.AddUserEvent;
import mops.gruppen2.domain.event.DeleteGroupEvent;
import mops.gruppen2.domain.event.DeleteUserEvent;
import mops.gruppen2.domain.event.UpdateGroupDescriptionEvent;
import mops.gruppen2.domain.event.UpdateGroupTitleEvent;
import mops.gruppen2.domain.event.UpdateRoleEvent;
import mops.gruppen2.domain.event.UpdateUserMaxEvent;
import mops.gruppen2.domain.exception.EventException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static mops.gruppen2.domain.Role.ADMIN;

/**
 * Behandelt Aufgaben, welche sich auf eine Gruppe beziehen
 */
@Service
public class GroupService {

    private final EventStoreService eventStoreService;
    private final ValidationService validationService;
    private final InviteService inviteService;
    private final ProjectionService projectionService;

    private static final Logger LOG = LoggerFactory.getLogger(GroupService.class);

    public GroupService(EventStoreService eventStoreService, ValidationService validationService, InviteService inviteService, ProjectionService projectionService) {
        this.eventStoreService = eventStoreService;
        this.validationService = validationService;
        this.inviteService = inviteService;
        this.projectionService = projectionService;
    }

    static User getVeteranMember(Account account, Group group) {
        List<User> members = group.getMembers();
        String newAdminId;
        if (members.get(0).getId().equals(account.getName())) {
            newAdminId = members.get(1).getId();
        } else {
            newAdminId = members.get(0).getId();
        }
        return new User(newAdminId, "", "", "");
    }

    /**
     * Wenn die maximale Useranzahl unendlich ist, wird das Maximum auf 100000 gesetzt. Praktisch gibt es also Maximla 100000
     * Nutzer pro Gruppe.
     *
     * @param isMaximumInfinite Gibt an ob es unendlich viele User geben soll
     * @param userMaximum       Das Maximum an Usern, falls es eins gibt
     *
     * @return Maximum an Usern
     */
    static Long checkInfiniteUsers(Boolean isMaximumInfinite, Long userMaximum) {
        isMaximumInfinite = isMaximumInfinite != null;

        if (isMaximumInfinite) {
            userMaximum = 100_000L;
        }

        return userMaximum;
    }

    static void removeOldUsersFromNewUsers(List<User> oldUsers, List<User> newUsers) {
        for (User oldUser : oldUsers) {
            newUsers.remove(oldUser);
        }
    }

    static Long adjustUserMaximum(Long newUsers, Long oldUsers, Long maxUsers) {
        if (oldUsers + newUsers > maxUsers) {
            maxUsers = oldUsers + newUsers;
        }
        return maxUsers;
    }

    static Visibility setGroupVisibility(Boolean isVisibilityPrivate) {
        isVisibilityPrivate = isVisibilityPrivate != null;

        if (isVisibilityPrivate) {
            return Visibility.PRIVATE;
        } else {
            return Visibility.PUBLIC;
        }
    }

    static GroupType setGroupType(Boolean isLecture) {
        isLecture = isLecture != null;
        if (isLecture) {
            return GroupType.LECTURE;
        } else {
            return GroupType.SIMPLE;
        }
    }

    //TODO: GroupService/eventbuilderservice
    void addUserList(List<User> newUsers, UUID groupId) {
        for (User user : newUsers) {
            Group group = projectionService.projectSingleGroup(groupId);
            if (group.getMembers().contains(user)) {
                LOG.info("Benutzer {} ist bereits in Gruppe", user.getId());
            } else {
                AddUserEvent addUserEvent = new AddUserEvent(groupId, user.getId(), user.getGivenname(), user.getFamilyname(), user.getEmail());
                eventStoreService.saveEvent(addUserEvent);
            }
        }
    }

    //TODO: GroupService/eventbuilderservice
    public void addUser(Account account, UUID groupId) {
        AddUserEvent addUserEvent = new AddUserEvent(groupId, account.getName(), account.getGivenname(), account.getFamilyname(), account.getEmail());
        eventStoreService.saveEvent(addUserEvent);
    }

    //TODO: GroupService/eventbuilderservice
    void updateTitle(Account account, UUID groupId, String title) {
        UpdateGroupTitleEvent updateGroupTitleEvent = new UpdateGroupTitleEvent(groupId, account.getName(), title);
        eventStoreService.saveEvent(updateGroupTitleEvent);
    }

    //TODO: GroupService/eventbuilderservice
    public void updateRole(User user, UUID groupId) throws EventException {
        UpdateRoleEvent updateRoleEvent;
        Group group = projectionService.projectSingleGroup(groupId);
        validationService.throwIfNotInGroup(group, user);

        if (group.getRoles().get(user.getId()) == ADMIN) {
            updateRoleEvent = new UpdateRoleEvent(group.getId(), user.getId(), Role.MEMBER);
        } else {
            updateRoleEvent = new UpdateRoleEvent(group.getId(), user.getId(), ADMIN);
        }
        eventStoreService.saveEvent(updateRoleEvent);
    }

    //TODO: GroupService/eventbuilderservice
    void updateDescription(Account account, UUID groupId, String description) {
        UpdateGroupDescriptionEvent updateGroupDescriptionEvent = new UpdateGroupDescriptionEvent(groupId, account.getName(), description);
        eventStoreService.saveEvent(updateGroupDescriptionEvent);
    }

    //TODO: GroupService
    public void addUsersFromCsv(Account account, MultipartFile file, String groupId) {
        Group group = projectionService.projectSingleGroup(UUID.fromString(groupId));

        List<User> newUserList = CsvService.readCsvFile(file);
        removeOldUsersFromNewUsers(group.getMembers(), newUserList);

        UUID groupUUID = IdService.stringToUUID(groupId);

        Long newUserMaximum = adjustUserMaximum((long) newUserList.size(), (long) group.getMembers().size(), group.getUserMaximum());
        if (newUserMaximum > group.getUserMaximum()) {
            updateMaxUser(account, groupUUID, newUserMaximum);
        }

        addUserList(newUserList, groupUUID);
    }

    //TODO: GroupService/eventbuilderservice
    public void updateMaxUser(Account account, UUID groupId, Long userMaximum) {
        UpdateUserMaxEvent updateUserMaxEvent = new UpdateUserMaxEvent(groupId, account.getName(), userMaximum);
        eventStoreService.saveEvent(updateUserMaxEvent);
    }

    //TODO: GroupService
    public void changeMetaData(Account account, Group group, String title, String description) {
        if (!title.equals(group.getTitle())) {
            updateTitle(account, group.getId(), title);
        }

        if (!description.equals(group.getDescription())) {
            updateDescription(account, group.getId(), description);
        }
    }

    //TODO: GroupService oder in Group?
    public Group getParent(UUID parentId) {
        Group parent = new Group();
        if (!IdService.idIsEmpty(parentId)) {
            parent = projectionService.projectSingleGroup(parentId);
        }
        return parent;
    }

    //TODO: GroupService
    public void deleteUser(Account account, User user, Group group) throws EventException {
        changeRoleIfLastAdmin(account, group);

        validationService.throwIfNotInGroup(group, user);

        deleteUserEvent(user, group.getId());

        if (validationService.checkIfGroupEmpty(group.getId())) {
            deleteGroupEvent(user.getId(), group.getId());
        }
    }

    //TODO: GroupService/eventbuilderservice
    private void deleteUserEvent(User user, UUID groupId) {
        DeleteUserEvent deleteUserEvent = new DeleteUserEvent(groupId, user.getId());
        eventStoreService.saveEvent(deleteUserEvent);
    }

    //TODO: GroupService/eventbuilderservice
    public void deleteGroupEvent(String userId, UUID groupId) {
        DeleteGroupEvent deleteGroupEvent = new DeleteGroupEvent(groupId, userId);
        inviteService.destroyLink(groupId);
        eventStoreService.saveEvent(deleteGroupEvent);
    }

    //TODO: GroupService
    private void promoteVeteranMember(Account account, Group group) {
        if (validationService.checkIfLastAdmin(account, group)) {
            User newAdmin = getVeteranMember(account, group);
            updateRole(newAdmin, group.getId());
        }
    }

    //TODO: GroupService
    public void changeRoleIfLastAdmin(Account account, Group group) {
        if (group.getMembers().size() <= 1) {
            return;
        }
        promoteVeteranMember(account, group);
    }

    //TODO: GroupService
    public void changeRole(Account account, User user, Group group) {
        if (user.getId().equals(account.getName())) {
            if (group.getMembers().size() <= 1) {
                validationService.throwIfLastAdmin(account, group);
            }
            promoteVeteranMember(account, group);
        }
        updateRole(user, group.getId());
    }

}
