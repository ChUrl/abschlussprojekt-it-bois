package mops.gruppen2.domain.event;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class DeleteUserEvent extends Event {

    public DeleteUserEvent(Group group, User user) {
        super(group.getGroupid(), user.getUserid());
    }

    @Override
    protected void applyEvent(Group group) throws EventException {
        ValidationHelper.throwIfNoMember(group, new User(userid));

        group.getMembers().remove(userid);
        group.getRoles().remove(userid);

        log.trace("\t\t\t\t\tNeue Members: {}", group.getMembers());
        log.trace("\t\t\t\t\tNeue Rollen: {}", group.getRoles());
    }
}
