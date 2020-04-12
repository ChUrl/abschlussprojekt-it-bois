package mops.gruppen2.domain.event;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.exception.BadParameterException;
import mops.gruppen2.domain.exception.EventException;
import mops.gruppen2.domain.model.Group;
import mops.gruppen2.domain.model.Limit;
import mops.gruppen2.domain.model.User;

@Getter
@ToString
@Log4j2
public class UpdateUserLimitEvent extends Event {

    private Limit userLimit;

    private UpdateUserLimitEvent() {}

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
