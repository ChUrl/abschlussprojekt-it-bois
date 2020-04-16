package mops.gruppen2.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.exception.LastAdminException;
import mops.gruppen2.domain.exception.UserNotFoundException;
import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.domain.model.group.Role;
import mops.gruppen2.infrastructure.GroupCache;

import java.util.UUID;

/**
 * Aktualisiert die Gruppenrolle eines Teilnehmers.
 */
@Log4j2
@Value
@AllArgsConstructor
public class UpdateRoleEvent extends Event {

    @JsonProperty("role")
    Role role;

    public UpdateRoleEvent(UUID groupId, String exec, String target, Role role) {
        super(groupId, exec, target);
        this.role = role;
    }

    @Override
    protected void updateCache(GroupCache cache, Group group) {}

    @Override
    protected void applyEvent(Group group) throws UserNotFoundException, LastAdminException {
        group.memberPutRole(target, role);

        log.trace("\t\t\t\t\tNeue Admin: {}", group.getAdmins());
    }

    @Override
    public String type() {
        return EventType.UPDATEROLE.toString();
    }

}
