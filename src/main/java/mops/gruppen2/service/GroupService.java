package mops.gruppen2.service;

import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.Group;
import mops.gruppen2.domain.GroupType;
import mops.gruppen2.domain.Role;
import mops.gruppen2.domain.User;
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

import java.util.List;
import java.util.UUID;

/**
 * Behandelt Aufgaben, welche sich auf eine Gruppe beziehen.
 * Es werden übergebene Gruppen bearbeitet und dementsprechend Events erzeugt und gespeichert.
 */
@Service
@Log4j2
public class GroupService {

    private final EventStoreService eventStoreService;
    private final InviteService inviteService;

    public GroupService(EventStoreService eventStoreService, InviteService inviteService) {
        this.eventStoreService = eventStoreService;
        this.inviteService = inviteService;
    }


    // ################################# GRUPPE ERSTELLEN ########################################


    /**
     * Erzeugt eine neue Gruppe und erzeugt nötige Events für die Initiale Setzung der Attribute.
     *
     * @param user        Keycloak-Account
     * @param title       Gruppentitel
     * @param description Gruppenbeschreibung
     */
    public Group createGroup(User user,
                             String title,
                             String description,
                             GroupType groupType,
                             long userLimit,
                             UUID parent) {

        // Regeln:
        // isPrivate -> !isLecture
        // isLecture -> !isPrivate
        Group group = createGroup(user, parent, groupType);

        // Die Reihenfolge ist wichtig, da der ausführende User Admin sein muss
        addUser(user, group);
        updateRole(user, group, Role.ADMIN);
        updateTitle(user, group, title);
        updateDescription(user, group, description);
        updateUserLimit(user, group, userLimit);

        inviteService.createLink(group);

        return group;
    }


    // ################################### GRUPPEN ÄNDERN ########################################


    /**
     * Fügt eine Liste von Usern zu einer Gruppe hinzu.
     * Duplikate werden übersprungen, die erzeugten Events werden gespeichert.
     * Dabei wird das Teilnehmermaximum eventuell angehoben.
     * Prüft, ob der User Admin ist.
     *
     * @param newUsers Userliste
     * @param group    Gruppe
     * @param user     Ausführender User
     */
    public void addUsersToGroup(List<User> newUsers, Group group, User user) {
        updateUserLimit(user, group, getAdjustedUserLimit(newUsers, group));

        newUsers.forEach(newUser -> addUserSilent(newUser, group));
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
    private static long getAdjustedUserLimit(List<User> newUsers, Group group) {
        return Math.max((long) group.getMembers().size() + newUsers.size(), group.getUserLimit());
    }

    /**
     * Wechselt die Rolle eines Teilnehmers von Admin zu Member oder andersherum.
     * Überprüft, ob der User Mitglied ist und ob er der letzte Admin ist.
     *
     * @param user  Teilnehmer, welcher geändert wird
     * @param group Gruppe, in welcher sih der Teilnehmer befindet
     *
     * @throws EventException Falls der User nicht gefunden wird
     */
    public void toggleMemberRole(User user, Group group) throws EventException {
        ValidationService.throwIfNoMember(group, user);
        ValidationService.throwIfLastAdmin(user, group);

        Role role = group.getRoles().get(user.getId());
        updateRole(user, group, role.toggle());
    }


    // ################################# SINGLE EVENTS ###########################################
    // Spezifische Events werden erzeugt, validiert, auf die Gruppe angewandt und gespeichert


    /**
     * Erzeugt eine Gruppe, speichert diese und gibt diese zurück.
     */
    private Group createGroup(User user, UUID parent, GroupType groupType) {
        Event event = new CreateGroupEvent(UUID.randomUUID(),
                                           user.getId(),
                                           parent,
                                           groupType);
        Group group = new Group();
        event.apply(group);

        eventStoreService.saveEvent(event);

        return group;
    }

    /**
     * Erzeugt, speichert ein AddUserEvent und wendet es auf eine Gruppe an.
     * Prüft, ob der Nutzer schon Mitglied ist und ob Gruppe voll ist.
     */
    public void addUser(User user, Group group) {
        ValidationService.throwIfMember(group, user);
        ValidationService.throwIfGroupFull(group);

        Event event = new AddUserEvent(group, user);
        event.apply(group);

        eventStoreService.saveEvent(event);
    }

    /**
     * Dasselbe wie addUser(), aber exceptions werden abgefangen und nicht geworfen.
     */
    private void addUserSilent(User user, Group group) {
        try {
            addUser(user, group);
        } catch (Exception e) {
            log.debug("Doppelter User {} wurde nicht zu Gruppe {} hinzugefügt!", user, group);
        }
    }

    /**
     * Erzeugt, speichert ein DeleteUserEvent und wendet es auf eine Gruppe an.
     * Prüft, ob der Nutzer Mitglied ist und ob er der letzte Admin ist.
     */
    public void deleteUser(User user, Group group) throws EventException {
        ValidationService.throwIfNoMember(group, user);
        ValidationService.throwIfLastAdmin(user, group);

        if (ValidationService.checkIfGroupEmpty(group)) {
            deleteGroup(user, group);
        } else {
            Event event = new DeleteUserEvent(group, user);
            event.apply(group);

            eventStoreService.saveEvent(event);
        }
    }

    /**
     * Erzeugt, speichert ein DeleteGroupEvent und wendet es auf eine Gruppe an.
     * Prüft, ob der Nutzer Admin ist.
     */
    public void deleteGroup(User user, Group group) {
        ValidationService.throwIfNoAdmin(group, user);

        Event event = new DeleteGroupEvent(group, user);
        event.apply(group);
        inviteService.destroyLink(group);

        eventStoreService.saveEvent(event);
    }

    /**
     * Erzeugt, speichert ein UpdateTitleEvent und wendet es auf eine Gruppe an.
     * Prüft, ob der Nutzer Admin ist und ob der Titel valide ist.
     * Bei keiner Änderung wird nichts erzeugt.
     */
    public void updateTitle(User user, Group group, String title) {
        ValidationService.throwIfNoAdmin(group, user);
        ValidationService.validateTitle(title.trim());

        if (title.trim().equals(group.getTitle())) {
            return;
        }

        Event event = new UpdateGroupTitleEvent(group, user, title.trim());
        event.apply(group);

        eventStoreService.saveEvent(event);
    }

    /**
     * Erzeugt, speichert ein UpdateDescriptiopnEvent und wendet es auf eine Gruppe an.
     * Prüft, ob der Nutzer Admin ist und ob die Beschreibung valide ist.
     * Bei keiner Änderung wird nichts erzeugt.
     */
    public void updateDescription(User user, Group group, String description) {
        ValidationService.throwIfNoAdmin(group, user);
        ValidationService.validateDescription(description.trim());

        if (description.trim().equals(group.getDescription())) {
            return;
        }

        Event event = new UpdateGroupDescriptionEvent(group, user, description.trim());
        event.apply(group);

        eventStoreService.saveEvent(event);
    }

    /**
     * Erzeugt, speichert ein UpdateRoleEvent und wendet es auf eine Gruppe an.
     * Prüft, ob der Nutzer Mitglied ist.
     * Bei keiner Änderung wird nichts erzeugt.
     */
    private void updateRole(User user, Group group, Role role) {
        ValidationService.throwIfNoMember(group, user);

        if (role == group.getRoles().get(user.getId())) {
            return;
        }

        Event event = new UpdateRoleEvent(group, user, role);
        event.apply(group);

        eventStoreService.saveEvent(event);
    }

    /**
     * Erzeugt, speichert ein UpdateUserLimitEvent und wendet es auf eine Gruppe an.
     * Prüft, ob der Nutzer Admin ist und ob das Limit valide ist.
     * Bei keiner Änderung wird nichts erzeugt.
     */
    public void updateUserLimit(User user, Group group, long userLimit) {
        ValidationService.throwIfNoAdmin(group, user);
        ValidationService.validateUserLimit(userLimit, group);

        if (userLimit == group.getUserLimit()) {
            return;
        }

        Event event = new UpdateUserLimitEvent(group, user, userLimit);
        event.apply(group);

        eventStoreService.saveEvent(event);
    }
}
