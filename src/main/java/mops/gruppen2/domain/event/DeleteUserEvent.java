package mops.gruppen2.domain.event;

import lombok.Value;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.exception.EventException;
import mops.gruppen2.domain.helper.ValidationHelper;
import mops.gruppen2.domain.model.Group;
import mops.gruppen2.domain.model.User;

/**
 * Entfernt ein einzelnes Mitglied einer Gruppe.
 */
@Log4j2
@Value
public class DeleteUserEvent extends Event {

    public DeleteUserEvent(Group group, User user) {
        super(group.getId(), user.getId());
    }

    @Override
    protected void applyEvent(Group group) throws EventException {
        ValidationHelper.throwIfNoMember(group, new User(userId));

        group.getMembers().remove(userId);
        group.getRoles().remove(userId);

        log.trace("\t\t\t\t\tNeue Members: {}", group.getMembers());
        log.trace("\t\t\t\t\tNeue Rollen: {}", group.getRoles());
    }
}
