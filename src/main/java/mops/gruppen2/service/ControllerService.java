package mops.gruppen2.service;

import mops.gruppen2.domain.Account;
import mops.gruppen2.domain.Group;
import mops.gruppen2.domain.GroupType;
import mops.gruppen2.domain.Role;
import mops.gruppen2.domain.User;
import mops.gruppen2.domain.Visibility;
import mops.gruppen2.domain.event.AddUserEvent;
import mops.gruppen2.domain.event.CreateGroupEvent;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static mops.gruppen2.domain.Role.ADMIN;


@Service
public class ControllerService {

    private static final Logger LOG = LoggerFactory.getLogger("controllerServiceLogger");
    private final EventStoreService eventStoreService;
    private final UserService userService;
    private final ValidationService validationService;
    private final InviteService inviteService;

    public ControllerService(EventStoreService eventStoreService, UserService userService, ValidationService validationService, InviteService inviteService) {
        this.eventStoreService = eventStoreService;
        this.userService = userService;
        this.validationService = validationService;
        this.inviteService = inviteService;
    }

    /**
     * Wie createGroup, nur das hier die Gruppe auch als Veranstaltung gesetzt werden kann und CSV Dateien mit Nutzern
     * eingelesen werden können.
     *
     * @param account             Der Nutzer der die Gruppe erstellt
     * @param title               Parameter für die neue Gruppe
     * @param description         Parameter für die neue Gruppe
     * @param isVisibilityPrivate Parameter für die neue Gruppe
     * @param isLecture           Parameter für die neue Gruppe
     * @param isMaximumInfinite   Parameter für die neue Gruppe
     * @param userMaximum         Parameter für die neue Gruppe
     * @param parent              Parameter für die neue Gruppe
     * @param file                Parameter für die neue Gruppe
     */
    //TODO: remove booleans + add wrapper?
    //TODO: auslagern teilweise -> EventBuilderService
    public void createGroupAsOrga(Account account,
                                  String title,
                                  String description,
                                  Boolean isVisibilityPrivate,
                                  Boolean isLecture,
                                  Boolean isMaximumInfinite,
                                  Long userMaximum,
                                  UUID parent,
                                  MultipartFile file) {

        userMaximum = GroupService.checkInfiniteUsers(isMaximumInfinite, userMaximum);

        List<User> newUsers = CsvService.readCsvFile(file);

        List<User> oldUsers = new ArrayList<>();
        User user = new User(account);
        oldUsers.add(user);

        GroupService.removeOldUsersFromNewUsers(oldUsers, newUsers);

        userMaximum = GroupService.adjustUserMaximum((long) newUsers.size(), 1L, userMaximum);

        UUID groupId = createGroup(account,
                                   title,
                                   description,
                                   isVisibilityPrivate,
                                   isLecture,
                                   isMaximumInfinite,
                                   userMaximum, parent);

        addUserList(newUsers, groupId);
    }

    /**
     * Erzeugt eine neue Gruppe, fügt den User, der die Gruppe erstellt hat, hinzu und setzt seine Rolle als Admin fest.
     * Zudem wird der Gruppentitel und die Gruppenbeschreibung erzeugt, welche vorher der Methode übergeben wurden.
     * Aus diesen Event-Objekten wird eine Liste erzeugt, welche daraufhin mithilfe des EventServices gesichert wird.
     *
     * @param account     Keycloak-Account
     * @param title       Gruppentitel
     * @param description Gruppenbeschreibung
     */
    //TODO: remove booleans + add wrapper?
    //TODO: auslagern teilweise -> EventBuilderService
    public UUID createGroup(Account account,
                            String title,
                            String description,
                            Boolean isVisibilityPrivate,
                            Boolean isLecture,
                            Boolean isMaximumInfinite,
                            Long userMaximum,
                            UUID parent) {

        userMaximum = GroupService.checkInfiniteUsers(isMaximumInfinite, userMaximum);

        Visibility groupVisibility = GroupService.setGroupVisibility(isVisibilityPrivate);
        UUID groupId = UUID.randomUUID();

        GroupType groupType = GroupService.setGroupType(isLecture);

        CreateGroupEvent createGroupEvent = new CreateGroupEvent(groupId,
                                                                 account.getName(),
                                                                 parent,
                                                                 groupType,
                                                                 groupVisibility,
                                                                 userMaximum);
        eventStoreService.saveEvent(createGroupEvent);

        inviteService.createLink(groupId);

        User user = new User(account.getName(), "", "", "");

        addUser(account, groupId);
        updateTitle(account, groupId, title);
        updateDescription(account, groupId, description);
        updateRole(user, groupId);

        return groupId;
    }

    //TODO: GroupService/eventbuilderservice
    private void addUserList(List<User> newUsers, UUID groupId) {
        for (User user : newUsers) {
            Group group = userService.getGroupById(groupId);
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
    private void updateTitle(Account account, UUID groupId, String title) {
        UpdateGroupTitleEvent updateGroupTitleEvent = new UpdateGroupTitleEvent(groupId, account.getName(), title);
        eventStoreService.saveEvent(updateGroupTitleEvent);
    }

    //TODO: GroupService/eventbuilderservice
    public void updateRole(User user, UUID groupId) throws EventException {
        UpdateRoleEvent updateRoleEvent;
        Group group = userService.getGroupById(groupId);
        validationService.throwIfNotInGroup(group, user);

        if (group.getRoles().get(user.getId()) == ADMIN) {
            updateRoleEvent = new UpdateRoleEvent(group.getId(), user.getId(), Role.MEMBER);
        } else {
            updateRoleEvent = new UpdateRoleEvent(group.getId(), user.getId(), ADMIN);
        }
        eventStoreService.saveEvent(updateRoleEvent);
    }

    //TODO: GroupService/eventbuilderservice
    private void updateDescription(Account account, UUID groupId, String description) {
        UpdateGroupDescriptionEvent updateGroupDescriptionEvent = new UpdateGroupDescriptionEvent(groupId, account.getName(), description);
        eventStoreService.saveEvent(updateGroupDescriptionEvent);
    }

    //TODO: GroupService
    public void addUsersFromCsv(Account account, MultipartFile file, String groupId) {
        Group group = userService.getGroupById(UUID.fromString(groupId));

        List<User> newUserList = CsvService.readCsvFile(file);
        GroupService.removeOldUsersFromNewUsers(group.getMembers(), newUserList);

        UUID groupUUID = getUUID(groupId);

        Long newUserMaximum = GroupService.adjustUserMaximum((long) newUserList.size(), (long) group.getMembers().size(), group.getUserMaximum());
        if (newUserMaximum > group.getUserMaximum()) {
            updateMaxUser(account, groupUUID, newUserMaximum);
        }

        addUserList(newUserList, groupUUID);
    }

    //TODO: GroupService
    public UUID getUUID(String id) {
        return UUID.fromString(Objects.requireNonNullElse(id, "00000000-0000-0000-0000-000000000000"));
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
        if (!GroupService.idIsEmpty(parentId)) {
            parent = userService.getGroupById(parentId);
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
            User newAdmin = GroupService.getVeteranMember(account, group);
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
