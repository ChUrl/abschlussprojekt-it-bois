package mops.gruppen2.domain.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.exception.BadArgumentException;
import mops.gruppen2.domain.exception.EventException;
import mops.gruppen2.domain.exception.IdMismatchException;
import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.infrastructure.GroupCache;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@Log4j2
@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "class")
@JsonSubTypes({@Type(value = AddMemberEvent.class, name = "ADDMEMBER"),
               @Type(value = CreateGroupEvent.class, name = "CREATEGROUP"),
               @Type(value = DestroyGroupEvent.class, name = "DESTROYGROUP"),
               @Type(value = KickMemberEvent.class, name = "KICKMEMBER"),
               @Type(value = SetDescriptionEvent.class, name = "SETDESCRIPTION"),
               @Type(value = SetInviteLinkEvent.class, name = "SETLINK"),
               @Type(value = SetLimitEvent.class, name = "SETLIMIT"),
               @Type(value = SetParentEvent.class, name = "SETPARENT"),
               @Type(value = SetTitleEvent.class, name = "SETTITLE"),
               @Type(value = SetTypeEvent.class, name = "SETTYPE"),
               @Type(value = UpdateRoleEvent.class, name = "UPDATEROLE")})
@Getter
@NoArgsConstructor // Lombok needs a default constructor in the base class
public abstract class Event {

    @JsonProperty("groupid")
    protected UUID groupid;

    @JsonProperty("version")
    protected long version; // Group-Version

    @JsonProperty("exec")
    protected String exec;

    @JsonProperty("target")
    protected String target;

    @JsonProperty("date")
    protected LocalDateTime date;

    public Event(UUID groupid, String exec, String target) {
        this.groupid = groupid;
        this.exec = exec;
        this.target = target;
    }

    public void init(long version) {
        if (this.version != 0) {
            throw new BadArgumentException("Event wurde schon initialisiert. (" + type() + ")");
        }
        date = LocalDateTime.now();

        log.trace("Event wurde initialisiert. (" + type() + "," + version + ")");

        this.version = version;
    }

    public void apply(Group group, GroupCache cache) throws EventException {
        log.trace("Event wird angewendet:\t{}", this);

        if (version == 0) {
            throw new BadArgumentException("Event wurde nicht initialisiert.");
        }

        checkGroupIdMatch(group.getId());
        applyEvent(group);
        updateCache(cache, group);

        // Danach hat die Gruppe nur Nullfelder
        if (this instanceof DestroyGroupEvent) {
            return;
        }

        group.updateVersion(version);
    }

    public void apply(Group group) throws EventException {
        log.trace("Event wird angewendet:\t{}", this);

        if (version == 0) {
            throw new BadArgumentException("Event wurde nicht initialisiert.");
        }

        checkGroupIdMatch(group.getId());
        group.updateVersion(version);
        applyEvent(group);
    }

    private void checkGroupIdMatch(UUID groupid) throws IdMismatchException {
        // CreateGroupEvents müssen die Id erst initialisieren
        if (this instanceof CreateGroupEvent) {
            return;
        }

        if (!this.groupid.equals(groupid)) {
            throw new IdMismatchException("Das Event gehört zu einer anderen Gruppe");
        }
    }

    protected abstract void updateCache(GroupCache cache, Group group);

    protected abstract void applyEvent(Group group) throws EventException;

    @JsonIgnore
    public abstract String format();

    @JsonIgnore
    public abstract String type();
}
