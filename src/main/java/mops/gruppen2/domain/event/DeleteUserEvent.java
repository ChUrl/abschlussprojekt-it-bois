package mops.gruppen2.domain.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.Group;
import mops.gruppen2.domain.User;
import mops.gruppen2.domain.exception.EventException;
import mops.gruppen2.domain.exception.UserNotFoundException;

import java.util.UUID;

/**
 * Entfernt ein einzelnes Mitglied einer Gruppe.
 */
@Getter
@NoArgsConstructor // For Jackson
@ToString
@Log4j2
public class DeleteUserEvent extends Event {

    public DeleteUserEvent(UUID groupId, String userId) {
        super(groupId, userId);
    }

    public DeleteUserEvent(Group group, User user) {
        super(group.getId(), user.getId());
    }

    //TODO: what the fuck use List.remove
    @Override
    protected void applyEvent(Group group) throws EventException {
        for (User user : group.getMembers()) {
            if (user.getId().equals(userId)) {
                group.getMembers().remove(user);
                group.getRoles().remove(user.getId());

                log.trace("\t\t\t\t\tNeue Members: {}", group.getMembers());
                log.trace("\t\t\t\t\tNeue Rollen: {}", group.getRoles());

                return;
            }
        }
        throw new UserNotFoundException(getClass().toString());
    }
}
