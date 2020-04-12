package mops.gruppen2.domain.event;

import lombok.Value;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.exception.BadParameterException;
import mops.gruppen2.domain.exception.EventException;
import mops.gruppen2.domain.model.Group;
import mops.gruppen2.domain.model.Limit;
import mops.gruppen2.domain.model.User;

@Log4j2
@Value
public class UpdateUserLimitEvent extends Event {

    Limit userLimit;

    public UpdateUserLimitEvent(Group group, User user, Limit userLimit) {
        super(group.getId(), user.getId());
        this.userLimit = userLimit;
    }

    @Override
    protected void applyEvent(Group group) throws EventException {
        if (userLimit.getUserLimit() < group.getMembers().size()) {
            throw new BadParameterException("Teilnehmerlimit zu klein.");
        }

        group.setUserLimit(userLimit);

        log.trace("\t\t\t\t\tNeues UserLimit: {}", group.getUserLimit());
    }
}
