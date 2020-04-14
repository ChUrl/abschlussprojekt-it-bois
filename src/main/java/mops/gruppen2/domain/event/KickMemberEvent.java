package mops.gruppen2.domain.event;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.exception.LastAdminException;
import mops.gruppen2.domain.exception.UserNotFoundException;
import mops.gruppen2.domain.model.group.Group;

/**
 * Entfernt ein einzelnes Mitglied einer Gruppe.
 */
@Log4j2
@Value
@AllArgsConstructor
public class KickMemberEvent extends Event {

    public KickMemberEvent(Group group, String exec, String target) {
        super(group.getId(), exec, target);
    }

    @Override
    protected void applyEvent(Group group) throws UserNotFoundException, LastAdminException {
        group.kickMember(target);

        log.trace("\t\t\t\t\tNeue Members: {}", group.getMembers());
    }

    @Override
    public String type() {
        return EventType.KICKMEMBER.toString();
    }
}
