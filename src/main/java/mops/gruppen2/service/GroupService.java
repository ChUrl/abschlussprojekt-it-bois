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
import mops.gruppen2.domain.event.Event;
import mops.gruppen2.domain.event.UpdateGroupDescriptionEvent;
import mops.gruppen2.domain.event.UpdateGroupTitleEvent;
import mops.gruppen2.domain.event.UpdateRoleEvent;
import mops.gruppen2.domain.event.UpdateUserLimitEvent;
import mops.gruppen2.domain.exception.EventException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static mops.gruppen2.domain.Role.ADMIN;

/**
 * Behandelt Aufgaben, welche sich auf eine Gruppe beziehen.
 * Es werden übergebene Gruppen bearbeitet und dementsprechend Events erzeugt und gespeichert.
 */
//TODO: Der GroupService sollte im Endeffekt größtenteils auf einer übergebenen Gruppe arbeiten.
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
     * Erzeugt eine neue Gruppe, fügt den User, der die Gruppe erstellt hat, hinzu und setzt seine Rolle als Admin fest.
     * Zudem wird der Gruppentitel und die Gruppenbeschreibung erzeugt, welche vorher der Methode übergeben wurden.
     * Aus diesen Event-Objekten wird eine Liste erzeugt, welche daraufhin mithilfe des EventServices gesichert wird.
     *
     * @param account     Keycloak-Account
     * @param title       Gruppentitel
     * @param description Gruppenbeschreibung
     */
    public Group createGroup(Account account,
                             String title,
                             String description,
                             Visibility visibility,
                             GroupType groupType,
                             long userLimit,
                             UUID parent) {

        UUID groupId = UUID.randomUUID();
        List<Event> events = new ArrayList<>();

        //TODO: etwas auslagern?
        events.add(new CreateGroupEvent(groupId,
                                        account.getName(),
                                        parent,
                                        groupType,
                                        visibility,
                                        userLimit));
        events.add(new AddUserEvent(groupId, new User(account)));
        events.add(new UpdateGroupTitleEvent(groupId, account.getName(), title));
        events.add(new UpdateGroupDescriptionEvent(groupId, account.getName(), description));
        events.add(new UpdateRoleEvent(groupId, account.getName(), ADMIN));

        inviteService.createLink(groupId);
        eventStoreService.saveAll(events);

        return ProjectionService.projectSingleGroup(events);
    }


    // ################################### GRUPPEN ÄNDERN ########################################


    /**
     * Fügt eine Liste von Usern zu einer Gruppe hinzu (in der Datenbank).
     * Duplikate werden übersprungen, die erzeugten Events werden gespeichert.
     * Dabei wird das Teilnehmermaximum eventuell angehoben.
     *
     * @param newUsers Userliste
     * @param group    Gruppe
     * @param account  Ausführender User
     */
    public void addUsersToGroup(List<User> newUsers, Group group, Account account) {
        updateUserLimit(account, group, getAdjustedUserLimit(newUsers, group));

        List<Event> events = newUsers.stream()
                                     .filter(user -> !group.getMembers().contains(user))
                                     .map(user -> new AddUserEvent(group.getId(), user))
                                     .collect(Collectors.toList());

        eventStoreService.saveAll(events);
    }

    void toggleMemberRole(User user, UUID groupId) throws EventException {
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

    public void deleteUser(Account account, User user, Group group) throws EventException {
        changeRoleIfLastAdmin(account, group);

        validationService.throwIfNotInGroup(group, user);

        deleteUser(user, group.getId());

        if (validationService.checkIfGroupEmpty(group.getId())) {
            deleteGroup(user.getId(), group.getId());
        }
    }

    private void promoteVeteranMember(Account account, Group group) {
        if (validationService.checkIfLastAdmin(account, group)) {
            User newAdmin = getVeteranMember(account, group);
            toggleMemberRole(newAdmin, group.getId());
        }
    }

    public void changeRoleIfLastAdmin(Account account, Group group) {
        if (group.getMembers().size() <= 1) {
            return;
        }
        promoteVeteranMember(account, group);
    }

    public void changeRole(Account account, User user, Group group) {
        if (user.getId().equals(account.getName())) {
            if (group.getMembers().size() <= 1) {
                validationService.throwIfLastAdmin(account, group);
            }
            promoteVeteranMember(account, group);
        }
        toggleMemberRole(user, group.getId());
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

    /**
     * Ermittelt ein passendes Teilnehmermaximum.
     * Reicht das alte Maximum, wird dieses zurückgegeben.
     * Ansonsten wird ein erhöhtes Maximum zurückgegeben.
     *
     * @param newUsers Neue Teilnehmer
     * @param group    Bestehende Gruppe, welche verändert wird
     *
     * @return Das neue Teilnehmermaximum
     */
    static long getAdjustedUserLimit(List<User> newUsers, Group group) {
        return Math.max(group.getMembers().size() + newUsers.size(),
                        group.getMembers().size());
    }


    // ################################# SINGLE EVENTS ###########################################


    public void deleteUser(User user, UUID groupId) {
        DeleteUserEvent event = new DeleteUserEvent(groupId, user.getId());
        eventStoreService.saveEvent(event);
    }

    public void deleteGroup(String userId, UUID groupId) {
        DeleteGroupEvent event = new DeleteGroupEvent(groupId, userId);
        inviteService.destroyLink(groupId);
        eventStoreService.saveEvent(event);
    }

    public void updateDescription(Account account, UUID groupId, String description) {
        UpdateGroupDescriptionEvent event = new UpdateGroupDescriptionEvent(groupId,
                                                                            account.getName(),
                                                                            description);
        eventStoreService.saveEvent(event);
    }

    public void updateUserLimit(Account account, Group group, long userLimit) {
        UpdateUserLimitEvent event = new UpdateUserLimitEvent(group.getId(),
                                                              account.getName(),
                                                              userLimit);
        eventStoreService.saveEvent(event);
    }

    public void addUser(Account account, UUID groupId) {
        AddUserEvent event = new AddUserEvent(groupId, new User(account));
        eventStoreService.saveEvent(event);
    }

    public void updateTitle(Account account, UUID groupId, String title) {
        UpdateGroupTitleEvent event = new UpdateGroupTitleEvent(groupId,
                                                                account.getName(),
                                                                title);
        eventStoreService.saveEvent(event);
    }

}
