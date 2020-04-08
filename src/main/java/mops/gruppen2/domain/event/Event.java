package mops.gruppen2.domain.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.Group;
import mops.gruppen2.domain.exception.EventException;
import mops.gruppen2.domain.exception.GroupIdMismatchException;

import java.util.UUID;


@Log4j2
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
@JsonSubTypes({
                      @JsonSubTypes.Type(value = AddUserEvent.class, name = "AddUserEvent"),
                      @JsonSubTypes.Type(value = CreateGroupEvent.class, name = "CreateGroupEvent"),
                      @JsonSubTypes.Type(value = DeleteUserEvent.class, name = "DeleteUserEvent"),
                      @JsonSubTypes.Type(value = UpdateGroupDescriptionEvent.class, name = "UpdateGroupDescriptionEvent"),
                      @JsonSubTypes.Type(value = UpdateGroupTitleEvent.class, name = "UpdateGroupTitleEvent"),
                      @JsonSubTypes.Type(value = UpdateRoleEvent.class, name = "UpdateRoleEvent"),
                      @JsonSubTypes.Type(value = DeleteGroupEvent.class, name = "DeleteGroupEvent"),
                      @JsonSubTypes.Type(value = UpdateUserLimitEvent.class, name = "UpdateUserLimitEvent")
              })
@Getter
@NoArgsConstructor
@AllArgsConstructor
public abstract class Event {

    protected UUID groupId;
    protected String userId;

    public Group apply(Group group) throws EventException {
        checkGroupIdMatch(group.getId());

        log.trace("Event angewendet:\t{}", this);

        applyEvent(group);

        return group;
    }

    private void checkGroupIdMatch(UUID groupId) {
        // CreateGroupEvents müssen die Id erst initialisieren
        if (this instanceof CreateGroupEvent) {
            return;
        }

        if (!this.groupId.equals(groupId)) {
            throw new GroupIdMismatchException(getClass().toString());
        }
    }

    protected abstract void applyEvent(Group group) throws EventException;
}
