package mops.gruppen2.domain.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.Group;
import mops.gruppen2.domain.Role;
import mops.gruppen2.domain.User;
import mops.gruppen2.domain.exception.UserNotFoundException;

import java.util.UUID;

/**
 * Aktualisiert die Gruppenrolle eines Teilnehmers.
 */
@Getter
@NoArgsConstructor // For Jackson
@ToString
@Log4j2
public class UpdateRoleEvent extends Event {

    private Role newRole;

    public UpdateRoleEvent(UUID groupId, String userId, Role newRole) {
        super(groupId, userId);
        this.newRole = newRole;
    }

    public UpdateRoleEvent(Group group, User user, Role newRole) {
        super(group.getId(), user.getId());
        this.newRole = newRole;
    }

    @Override
    protected void applyEvent(Group group) throws UserNotFoundException {
        if (group.getRoles().containsKey(userId)) {
            group.getRoles().put(userId, newRole);

            log.trace("\t\t\t\t\tNeue Rollen: {}", group.getRoles());

            return;
        }

        throw new UserNotFoundException(getClass().toString());
    }

}
