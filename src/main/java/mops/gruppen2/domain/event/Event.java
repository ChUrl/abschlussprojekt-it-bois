package mops.gruppen2.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.exception.EventException;
import mops.gruppen2.domain.exception.GroupIdMismatchException;
import mops.gruppen2.domain.model.Group;

import java.util.UUID;

@Log4j2
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = AddUserEvent.class, name = "AddUserEvent"),
               @JsonSubTypes.Type(value = CreateGroupEvent.class, name = "CreateGroupEvent"),
               @JsonSubTypes.Type(value = DeleteUserEvent.class, name = "DeleteUserEvent"),
               @JsonSubTypes.Type(value = UpdateGroupDescriptionEvent.class, name = "UpdateGroupDescriptionEvent"),
               @JsonSubTypes.Type(value = UpdateGroupTitleEvent.class, name = "UpdateGroupTitleEvent"),
               @JsonSubTypes.Type(value = UpdateRoleEvent.class, name = "UpdateRoleEvent"),
               @JsonSubTypes.Type(value = DeleteGroupEvent.class, name = "DeleteGroupEvent"),
               @JsonSubTypes.Type(value = UpdateUserLimitEvent.class, name = "UpdateUserLimitEvent")})
@Getter
@AllArgsConstructor
@NoArgsConstructor // Lombok needs a default constructor in the base class
public abstract class Event {

    @JsonProperty("groupid")
    protected UUID groupid;

    @JsonProperty("userid")
    protected String userid;

    public Group apply(Group group) throws EventException {
        checkGroupIdMatch(group.getGroupid());

        log.trace("Event angewendet:\t{}", this);

        applyEvent(group);

        return group;
    }

    private void checkGroupIdMatch(UUID groupId) {
        // CreateGroupEvents müssen die Id erst initialisieren
        if (this instanceof CreateGroupEvent) {
            return;
        }

        if (!groupid.equals(groupId)) {
            throw new GroupIdMismatchException(getClass().toString());
        }
    }

    protected abstract void applyEvent(Group group) throws EventException;
}
