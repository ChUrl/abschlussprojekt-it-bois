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

import java.util.UUID;

@Log4j2
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "class")
@JsonSubTypes({@JsonSubTypes.Type(value = AddMemberEvent.class, name = "ADDMEMBER"),
               @JsonSubTypes.Type(value = CreateGroupEvent.class, name = "CREATEGROUP"),
               @JsonSubTypes.Type(value = DestroyGroupEvent.class, name = "DESTROYGROUP"),
               @JsonSubTypes.Type(value = KickMemberEvent.class, name = "KICKMEMBER"),
               @JsonSubTypes.Type(value = SetDescriptionEvent.class, name = "SETDESCRIPTION"),
               @JsonSubTypes.Type(value = SetInviteLinkEvent.class, name = "SETLINK"),
               @JsonSubTypes.Type(value = SetLimitEvent.class, name = "SETLIMIT"),
               @JsonSubTypes.Type(value = SetParentEvent.class, name = "SETPARENT"),
               @JsonSubTypes.Type(value = SetTitleEvent.class, name = "SETTITLE"),
               @JsonSubTypes.Type(value = SetTypeEvent.class, name = "SETTYPE"),
               @JsonSubTypes.Type(value = UpdateRoleEvent.class, name = "UPDATEROLE")})
@Getter
@NoArgsConstructor // Lombok needs a default constructor in the base class
public abstract class Event {

    @JsonProperty("groupid")
    protected UUID groupid;

    @JsonProperty("version")
    protected long version;

    @JsonProperty("exec")
    protected String exec;

    @JsonProperty("target")
    protected String target;

    public Event(UUID groupid, String exec, String target) {
        this.groupid = groupid;
        this.exec = exec;
        this.target = target;
    }

    public void init(long version) {
        if (this.version != 0) {
            throw new BadArgumentException("Event wurde schon initialisiert. (" + type() + ")");
        }

        log.trace("Event wurde initialisiert. (" + type() + "," + version + ")");

        this.version = version;
    }

    public void apply(Group group, GroupCache cache) throws EventException {
        log.trace("Event wird angewendet:\t{}", this);

        if (version == 0) {
            throw new BadArgumentException("Event wurde nicht initialisiert.");
        }

        checkGroupIdMatch(group.getId());
        group.updateVersion(version);
        updateCache(cache, group);
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
    public abstract String type();
}
