package mops.gruppen2.domain.event;

import lombok.Getter;
import lombok.ToString;
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
@Getter
@ToString
@Log4j2
public class UpdateRoleEvent extends Event {

    private Role newRole;

    private UpdateRoleEvent() {}

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
