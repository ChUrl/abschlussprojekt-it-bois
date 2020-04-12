package mops.gruppen2.domain.event;

import lombok.Value;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.exception.UserNotFoundException;
import mops.gruppen2.domain.helper.ValidationHelper;
import mops.gruppen2.domain.model.Group;
import mops.gruppen2.domain.model.Role;
import mops.gruppen2.domain.model.User;

import java.util.UUID;

/**
 * Aktualisiert die Gruppenrolle eines Teilnehmers.
 */
@Log4j2
@Value
public class UpdateRoleEvent extends Event {

    Role newRole;

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
        ValidationHelper.throwIfNoMember(group, new User(userId));

        group.getRoles().put(userId, newRole);

        log.trace("\t\t\t\t\tNeue Rollen: {}", group.getRoles());
    }

}
