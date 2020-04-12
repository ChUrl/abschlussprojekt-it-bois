package mops.gruppen2.domain.service;

import lombok.extern.log4j.Log4j2;
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
import mops.gruppen2.domain.helper.ValidationHelper;
import mops.gruppen2.domain.model.Description;
import mops.gruppen2.domain.model.Group;
import mops.gruppen2.domain.model.Limit;
import mops.gruppen2.domain.model.Role;
import mops.gruppen2.domain.model.Title;
import mops.gruppen2.domain.model.Type;
import mops.gruppen2.domain.model.User;
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
                             Title title,
                             Description description,
                             Type type,
                             Limit userLimit,
                             UUID parent) {

        // Regeln:
        // isPrivate -> !isLecture
        // isLecture -> !isPrivate
        Group group = createGroup(user, parent, type);

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
    private static Limit getAdjustedUserLimit(List<User> newUsers, Group group) {
        return new Limit(Math.max((long) group.getMembers().size() + newUsers.size(), group.getLimit().getUserLimit()));
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
        ValidationHelper.throwIfNoMember(group, user);
        ValidationHelper.throwIfLastAdmin(user, group);

        Role role = group.getRoles().get(user.getUserid());
        updateRole(user, group, role.toggle());
    }


    // ################################# SINGLE EVENTS ###########################################
    // Spezifische Events werden erzeugt, validiert, auf die Gruppe angewandt und gespeichert


    /**
     * Erzeugt eine Gruppe, speichert diese und gibt diese zurück.
     */
    private Group createGroup(User user, UUID parent, Type type) {
        Event event = new CreateGroupEvent(UUID.randomUUID(),
                                           user,
                                           parent,
                                           type);
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
        ValidationHelper.throwIfMember(group, user);
        ValidationHelper.throwIfGroupFull(group);

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
        ValidationHelper.throwIfNoMember(group, user);
        ValidationHelper.throwIfLastAdmin(user, group);

        if (ValidationHelper.checkIfGroupEmpty(group)) {
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
        ValidationHelper.throwIfNoAdmin(group, user);

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
    public void updateTitle(User user, Group group, Title title) {
        ValidationHelper.throwIfNoAdmin(group, user);

        if (title.equals(group.getTitle())) {
            return;
        }

        Event event = new UpdateGroupTitleEvent(group, user, title);
        event.apply(group);

        eventStoreService.saveEvent(event);
    }

    /**
     * Erzeugt, speichert ein UpdateDescriptiopnEvent und wendet es auf eine Gruppe an.
     * Prüft, ob der Nutzer Admin ist und ob die Beschreibung valide ist.
     * Bei keiner Änderung wird nichts erzeugt.
     */
    public void updateDescription(User user, Group group, Description description) {
        ValidationHelper.throwIfNoAdmin(group, user);

        if (description.equals(group.getDescription())) {
            return;
        }

        Event event = new UpdateGroupDescriptionEvent(group, user, description);
        event.apply(group);

        eventStoreService.saveEvent(event);
    }

    /**
     * Erzeugt, speichert ein UpdateRoleEvent und wendet es auf eine Gruppe an.
     * Prüft, ob der Nutzer Mitglied ist.
     * Bei keiner Änderung wird nichts erzeugt.
     */
    private void updateRole(User user, Group group, Role role) {
        ValidationHelper.throwIfNoMember(group, user);

        if (role == group.getRoles().get(user.getUserid())) {
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
    public void updateUserLimit(User user, Group group, Limit userLimit) {
        ValidationHelper.throwIfNoAdmin(group, user);

        if (userLimit == group.getLimit()) {
            return;
        }

        Event event;
        if (userLimit.getUserLimit() < group.getMembers().size()) {
            event = new UpdateUserLimitEvent(group, user, new Limit(group.getMembers().size()));
        } else {
            event = new UpdateUserLimitEvent(group, user, userLimit);
        }

        event.apply(group);

        eventStoreService.saveEvent(event);
    }
}
