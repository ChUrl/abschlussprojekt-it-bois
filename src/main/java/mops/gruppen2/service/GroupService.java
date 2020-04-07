package mops.gruppen2.service;

import lombok.extern.log4j.Log4j2;
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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static mops.gruppen2.domain.Role.ADMIN;

/**
 * Behandelt Aufgaben, welche sich auf eine Gruppe beziehen
 */
@Service
@Log4j2
public class GroupService {

    private final EventStoreService eventStoreService;
    private final ValidationService validationService;
    private final InviteService inviteService;
    private final ProjectionService projectionService;

    public GroupService(EventStoreService eventStoreService, ValidationService validationService, InviteService inviteService, ProjectionService projectionService) {
        this.eventStoreService = eventStoreService;
        this.validationService = validationService;
        this.inviteService = inviteService;
        this.projectionService = projectionService;
    }


    // ################################# GRUPPE ERSTELLEN ########################################


    /**
     * Wie createGroup, nur das hier die Gruppe auch als Veranstaltung gesetzt werden kann und CSV Dateien mit Nutzern
     * eingelesen werden können.
     *
     * @param account     Der Nutzer der die Gruppe erstellt
     * @param title       Parameter für die neue Gruppe
     * @param description Parameter für die neue Gruppe
     * @param visibility  Parameter für die neue Gruppe
     * @param userMaximum Parameter für die neue Gruppe
     * @param parent      Parameter für die neue Gruppe
     * @param file        Parameter für die neue Gruppe
     */
    //TODO: add wrapper (GroupMeta)?
    //TODO: auslagern teilweise -> EventBuilderService
    public void createGroupAsOrga(Account account,
                                  String title,
                                  String description,
                                  Visibility visibility,
                                  GroupType groupType,
                                  long userMaximum,
                                  UUID parent,
                                  MultipartFile file) {

        // CSV-Import
        List<User> newUsers = CsvService.readCsvFile(file);
        newUsers.remove(new User(account));
        long newUserMaximum = adjustUserMaximum(newUsers.size(), 1L, userMaximum);

        UUID groupId = createGroup(account,
                                   title,
                                   description,
                                   visibility,
                                   groupType,
                                   newUserMaximum,
                                   parent);

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
    //TODO: add wrapper?
    //TODO: auslagern teilweise -> EventBuilderService
    public UUID createGroup(Account account,
                            String title,
                            String description,
                            Visibility visibility,
                            GroupType groupType,
                            Long userMaximum,
                            UUID parent) {

        UUID groupId = UUID.randomUUID();

        CreateGroupEvent createGroupEvent = new CreateGroupEvent(groupId,
                                                                 account.getName(),
                                                                 parent,
                                                                 groupType,
                                                                 visibility,
                                                                 userMaximum);

        eventStoreService.saveEvent(createGroupEvent);

        inviteService.createLink(groupId);

        User user = new User(account);

        addUser(account, groupId);
        updateTitle(account, groupId, title);
        updateDescription(account, groupId, description);
        updateRole(user, groupId);

        return groupId;
    }


    // ################################ GRUPPENMANIPULATION ######################################


    //TODO: GroupService/eventbuilderservice
    void addUserList(List<User> newUsers, UUID groupId) {
        Group group = projectionService.projectSingleGroup(groupId);

        for (User user : newUsers) {
            if (group.getMembers().contains(user)) {
                log.info("Benutzer {} ist bereits in Gruppe", user.getId());
            } else {
                AddUserEvent addUserEvent = new AddUserEvent(groupId, user);
                eventStoreService.saveEvent(addUserEvent);
            }
        }
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

    //TODO: GroupService
    public void addUsersFromCsv(Account account, MultipartFile file, String groupId) {
        Group group = projectionService.projectSingleGroup(UUID.fromString(groupId));

        List<User> newUserList = CsvService.readCsvFile(file);
        removeOldUsersFromNewUsers(group.getMembers(), newUserList);

        UUID groupUUID = IdService.stringToUUID(groupId);

        Long newUserMaximum = adjustUserMaximum(newUserList.size(),
                                                group.getMembers().size(),
                                                group.getUserMaximum());

        if (newUserMaximum > group.getUserMaximum()) {
            updateMaxUser(account, groupUUID, newUserMaximum);
        }

        addUserList(newUserList, groupUUID);
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

    //TODO: GroupService
    public void deleteUser(Account account, User user, Group group) throws EventException {
        changeRoleIfLastAdmin(account, group);

        validationService.throwIfNotInGroup(group, user);

        deleteUserEvent(user, group.getId());

        if (validationService.checkIfGroupEmpty(group.getId())) {
            deleteGroupEvent(user.getId(), group.getId());
        }
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


    // ############################### GRUPEN ANFRAGEN ###########################################


    static User getVeteranMember(Account account, Group group) {
        List<User> members = group.getMembers();
        String newAdminId;
        if (members.get(0).getId().equals(account.getName())) {
            newAdminId = members.get(1).getId();
        } else {
            newAdminId = members.get(0).getId();
        }
        return new User(newAdminId);
    }

    static long adjustUserMaximum(long newUsers, long oldUsers, long maxUsers) {
        return Math.max(oldUsers + newUsers, maxUsers);
    }

    private static void removeOldUsersFromNewUsers(List<User> oldUsers, List<User> newUsers) {
        for (User oldUser : oldUsers) {
            newUsers.remove(oldUser);
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


    //TODO: Eventbuilderservice
    // ################################### EVENTS ################################################


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

    //TODO: GroupService/eventbuilderservice
    void updateDescription(Account account, UUID groupId, String description) {
        UpdateGroupDescriptionEvent updateGroupDescriptionEvent = new UpdateGroupDescriptionEvent(groupId, account.getName(), description);
        eventStoreService.saveEvent(updateGroupDescriptionEvent);
    }

    //TODO: GroupService/eventbuilderservice
    public void updateMaxUser(Account account, UUID groupId, Long userMaximum) {
        UpdateUserMaxEvent updateUserMaxEvent = new UpdateUserMaxEvent(groupId, account.getName(), userMaximum);
        eventStoreService.saveEvent(updateUserMaxEvent);
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

}
