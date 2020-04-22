package mops.gruppen2;

import mops.gruppen2.domain.event.AddMemberEvent;
import mops.gruppen2.domain.event.CreateGroupEvent;
import mops.gruppen2.domain.event.DestroyGroupEvent;
import mops.gruppen2.domain.event.Event;
import mops.gruppen2.domain.event.KickMemberEvent;
import mops.gruppen2.domain.event.SetDescriptionEvent;
import mops.gruppen2.domain.event.SetInviteLinkEvent;
import mops.gruppen2.domain.event.SetLimitEvent;
import mops.gruppen2.domain.event.SetTitleEvent;
import mops.gruppen2.domain.event.SetTypeEvent;
import mops.gruppen2.domain.event.UpdateRoleEvent;
import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.domain.model.group.Role;
import mops.gruppen2.domain.model.group.Type;
import mops.gruppen2.domain.model.group.User;
import mops.gruppen2.domain.model.group.wrapper.Description;
import mops.gruppen2.domain.model.group.wrapper.Limit;
import mops.gruppen2.domain.model.group.wrapper.Link;
import mops.gruppen2.domain.model.group.wrapper.Title;
import mops.gruppen2.infrastructure.GroupCache;

import java.time.LocalDateTime;
import java.util.UUID;

import static mops.gruppen2.TestHelper.uuid;

public final class GroupBuilder {

    private final UUID groupid;
    private int version;
    private final GroupCache groupCache;
    private final Group group = Group.EMPTY();

    private GroupBuilder(GroupCache cache, UUID id) {
        groupCache = cache;
        groupid = id;
    }

    /**
     * Erzeugt neuen GruppenBuilder mit Cache und ID
     */
    public static GroupBuilder get(GroupCache cache, int id) {
        return new GroupBuilder(cache, uuid(id));
    }

    /**
     * Initialisiert Gruppe mit Id, Creator und Zeit
     */
    public GroupBuilder group() {
        return apply(new CreateGroupEvent(groupid, "TEST", LocalDateTime.now()));
    }

    /**
     * Initialisiert TestAdmin
     */
    public GroupBuilder testadmin() {
        apply(new AddMemberEvent(groupid, "TEST", "TEST", new User("TEST")));
        return apply(new UpdateRoleEvent(groupid, "TEST", "TEST", Role.ADMIN));
    }

    /**
     * Fügt Nutzer hinzu
     */
    public GroupBuilder add(String userid) {
        return apply(new AddMemberEvent(groupid, "TEST", userid, new User(userid)));
    }

    /**
     * Entfernt Nutzer
     */
    public GroupBuilder kick(String userid) {
        return apply(new KickMemberEvent(groupid, "TEST", userid));
    }

    public GroupBuilder limit(int i) {
        return apply(new SetLimitEvent(groupid, "TEST", new Limit(i)));
    }

    /**
     * Macht Nutzer zu Admin
     */
    public GroupBuilder admin(String userid) {
        return apply(new UpdateRoleEvent(groupid, "TEST", userid, Role.ADMIN));
    }

    /**
     * Macht Nutzer zu regulärem
     */
    public GroupBuilder regular(String userid) {
        return apply(new UpdateRoleEvent(groupid, "TEST", userid, Role.REGULAR));
    }

    /**
     * Macht Gruppe öffentlich
     */
    public GroupBuilder publik() {
        return apply(new SetTypeEvent(groupid, "TEST", Type.PUBLIC));
    }

    /**
     * Macht Gruppe privat
     */
    public GroupBuilder privat() {
        return apply(new SetTypeEvent(groupid, "TEST", Type.PRIVATE));
    }

    /**
     * Macht Gruppe zu Veranstaltung
     */
    public GroupBuilder lecture() {
        return apply(new SetTypeEvent(groupid, "TEST", Type.LECTURE));
    }

    /**
     * Setzt Beschreibung
     */
    public GroupBuilder desc(String descr) {
        return apply(new SetDescriptionEvent(groupid, "TEST", new Description(descr)));
    }

    /**
     * Setzt Titel
     */
    public GroupBuilder title(String titl) {
        return apply(new SetTitleEvent(groupid, "TEST", new Title(titl)));
    }

    /**
     * Setzt Link
     */
    public GroupBuilder link(int lnk) {
        return apply(new SetInviteLinkEvent(groupid, "TEST", new Link(uuid(lnk).toString())));
    }

    /**
     * Löscht Gruppe
     */
    public GroupBuilder destroy() {
        return apply(new DestroyGroupEvent(groupid, "TEST"));
    }

    public Group build() {
        return group;
    }

    private GroupBuilder apply(Event event) {
        version++;
        event.init(version);
        event.apply(group, groupCache);
        return this;
    }
}
