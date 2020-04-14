package mops.gruppen2.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.aspect.annotation.TraceMethodCalls;
import mops.gruppen2.domain.event.AddMemberEvent;
import mops.gruppen2.domain.event.CreateGroupEvent;
import mops.gruppen2.domain.event.DestroyGroupEvent;
import mops.gruppen2.domain.event.Event;
import mops.gruppen2.domain.event.KickMemberEvent;
import mops.gruppen2.domain.event.SetDescriptionEvent;
import mops.gruppen2.domain.event.SetInviteLinkEvent;
import mops.gruppen2.domain.event.SetLimitEvent;
import mops.gruppen2.domain.event.SetParentEvent;
import mops.gruppen2.domain.event.SetTitleEvent;
import mops.gruppen2.domain.event.SetTypeEvent;
import mops.gruppen2.domain.event.UpdateRoleEvent;
import mops.gruppen2.domain.exception.EventException;
import mops.gruppen2.domain.helper.ValidationHelper;
import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.domain.model.group.Role;
import mops.gruppen2.domain.model.group.Type;
import mops.gruppen2.domain.model.group.User;
import mops.gruppen2.domain.model.group.wrapper.Description;
import mops.gruppen2.domain.model.group.wrapper.Limit;
import mops.gruppen2.domain.model.group.wrapper.Link;
import mops.gruppen2.domain.model.group.wrapper.Parent;
import mops.gruppen2.domain.model.group.wrapper.Title;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Behandelt Aufgaben, welche sich auf eine Gruppe beziehen.
 * Es werden übergebene Gruppen bearbeitet und dementsprechend Events erzeugt und gespeichert.
 */
@Log4j2
@TraceMethodCalls
@RequiredArgsConstructor
@Service
public class GroupService {

    private final EventStoreService eventStoreService;

    // ################################# GRUPPE ERSTELLEN ########################################


    public Group createGroup(String exec) {
        return createGroup(UUID.randomUUID(), exec, LocalDateTime.now());
    }

    public void initGroupMembers(Group group,
                                 String exec,
                                 String target,
                                 User user,
                                 Limit limit) {

        addMember(group, exec, target, user);
        updateRole(group, exec, target, Role.ADMIN);
        setLimit(group, exec, limit);
    }

    public void initGroupMeta(Group group,
                              String exec,
                              Type type,
                              Parent parent) {

        setType(group, exec, type);
        setParent(group, exec, parent);
    }

    public void initGroupText(Group group,
                              String exec,
                              Title title,
                              Description description) {

        setTitle(group, exec, title);
        setDescription(group, exec, description);
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
     * @param exec     Ausführender User
     */
    public void addUsersToGroup(Group group, String exec, List<User> newUsers) {
        setLimit(group, exec, getAdjustedUserLimit(newUsers, group));

        newUsers.forEach(newUser -> addUserSilent(group, exec, newUser.getId(), newUser));
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
        return new Limit(Math.max((long) group.size() + newUsers.size(), group.getLimit()));
    }

    /**
     * Wechselt die Rolle eines Teilnehmers von Admin zu Member oder andersherum.
     * Überprüft, ob der User Mitglied ist und ob er der letzte Admin ist.
     *
     * @param target Teilnehmer, welcher geändert wird
     * @param group  Gruppe, in welcher sih der Teilnehmer befindet
     *
     * @throws EventException Falls der User nicht gefunden wird
     */
    public void toggleMemberRole(Group group, String exec, String target) {
        updateRole(group, exec, target, group.getRole(target).toggle());
    }


    // ################################# SINGLE EVENTS ###########################################
    // Spezifische Events werden erzeugt, validiert, auf die Gruppe angewandt und gespeichert


    /**
     * Erzeugt eine Gruppe, speichert diese und gibt diese zurück.
     */
    private Group createGroup(UUID groupid, String exec, LocalDateTime date) {
        Event event = new CreateGroupEvent(groupid,
                                           exec,
                                           date);
        Group group = new Group();
        applyAndSave(group, event);

        return group;
    }

    /**
     * Dasselbe wie addUser(), aber exceptions werden abgefangen und nicht geworfen.
     */
    private void addUserSilent(Group group, String exec, String target, User user) {
        try {
            addMember(group, exec, target, user);
        } catch (Exception e) {
            log.debug("Doppelter User {} wurde nicht zu Gruppe {} hinzugefügt!", user, group);
        }
    }

    /**
     * Erzeugt, speichert ein AddUserEvent und wendet es auf eine Gruppe an.
     * Prüft, ob der Nutzer schon Mitglied ist und ob Gruppe voll ist.
     */
    public void addMember(Group group, String exec, String target, User user) {
        applyAndSave(group, new AddMemberEvent(group, exec, target, user));
    }

    /**
     * Erzeugt, speichert ein DeleteUserEvent und wendet es auf eine Gruppe an.
     * Prüft, ob der Nutzer Mitglied ist und ob er der letzte Admin ist.
     */
    public void kickMember(Group group, String exec, String target) {
        applyAndSave(group, new KickMemberEvent(group, exec, target));

        if (ValidationHelper.checkIfGroupEmpty(group)) {
            deleteGroup(group, exec);
        }
    }

    /**
     * Erzeugt, speichert ein DeleteGroupEvent und wendet es auf eine Gruppe an.
     * Prüft, ob der Nutzer Admin ist.
     */
    public void deleteGroup(Group group, String exec) {
        if (!group.exists()) {
            return;
        }

        applyAndSave(group, new DestroyGroupEvent(group, exec));
    }

    /**
     * Erzeugt, speichert ein UpdateTitleEvent und wendet es auf eine Gruppe an.
     * Prüft, ob der Nutzer Admin ist und ob der Titel valide ist.
     * Bei keiner Änderung wird nichts erzeugt.
     */
    public void setTitle(Group group, String exec, Title title) {
        if (group.getTitle().equals(title.getValue())) {
            return;
        }

        applyAndSave(group, new SetTitleEvent(group, exec, title));
    }

    /**
     * Erzeugt, speichert ein UpdateDescriptiopnEvent und wendet es auf eine Gruppe an.
     * Prüft, ob der Nutzer Admin ist und ob die Beschreibung valide ist.
     * Bei keiner Änderung wird nichts erzeugt.
     */
    public void setDescription(Group group, String exec, Description description) {
        if (group.getDescription().equals(description.getValue())) {
            return;
        }

        applyAndSave(group, new SetDescriptionEvent(group, exec, description));
    }

    /**
     * Erzeugt, speichert ein UpdateRoleEvent und wendet es auf eine Gruppe an.
     * Prüft, ob der Nutzer Mitglied ist.
     * Bei keiner Änderung wird nichts erzeugt.
     */
    private void updateRole(Group group, String exec, String target, Role role) {
        if (group.memberHasRole(target, role)) {
            return;
        }

        applyAndSave(group, new UpdateRoleEvent(group, exec, target, role));
    }

    /**
     * Erzeugt, speichert ein UpdateUserLimitEvent und wendet es auf eine Gruppe an.
     * Prüft, ob der Nutzer Admin ist und ob das Limit valide ist.
     * Bei keiner Änderung wird nichts erzeugt.
     */
    public void setLimit(Group group, String exec, Limit userLimit) {
        if (userLimit.getValue() == group.getLimit()) {
            return;
        }

        applyAndSave(group, new SetLimitEvent(group, exec, userLimit));
    }

    public void setParent(Group group, String exec, Parent parent) {
        if (parent.getValue() == group.getParent()) {
            return;
        }

        applyAndSave(group, new SetParentEvent(group, exec, parent));
    }

    public void setLink(Group group, String exec, Link link) {
        if (group.getLink().equals(link.getValue())) {
            return;
        }

        applyAndSave(group, new SetInviteLinkEvent(group, exec, link));
    }

    private void setType(Group group, String exec, Type type) {
        if (group.getType() == type) {
            return;
        }

        applyAndSave(group, new SetTypeEvent(group, exec, type));
    }

    private void applyAndSave(Group group, Event event) throws EventException {
        event.init(group.version() + 1);
        event.apply(group);

        eventStoreService.saveEvent(event);
    }
}
