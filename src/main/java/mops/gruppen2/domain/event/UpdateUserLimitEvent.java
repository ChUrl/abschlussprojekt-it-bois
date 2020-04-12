package mops.gruppen2.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.exception.BadParameterException;
import mops.gruppen2.domain.exception.EventException;
import mops.gruppen2.domain.model.Group;
import mops.gruppen2.domain.model.Limit;
import mops.gruppen2.domain.model.User;

@Log4j2
@Value
@AllArgsConstructor
public class UpdateUserLimitEvent extends Event {

    @JsonProperty("limit")
    Limit limit;

    public UpdateUserLimitEvent(Group group, User user, Limit limit) {
        super(group.getGroupid(), user.getUserid());
        this.limit = limit;
    }

    @Override
    protected void applyEvent(Group group) throws EventException {
        if (limit.getUserLimit() < group.getMembers().size()) {
            throw new BadParameterException("Teilnehmerlimit zu klein.");
        }

        group.setLimit(limit);

        log.trace("\t\t\t\t\tNeues UserLimit: {}", group.getLimit());
    }
}
