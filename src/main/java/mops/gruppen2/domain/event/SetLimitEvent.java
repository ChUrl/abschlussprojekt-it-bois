package mops.gruppen2.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.exception.BadArgumentException;
import mops.gruppen2.domain.exception.NoAccessException;
import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.domain.model.group.wrapper.Limit;

import javax.validation.Valid;
import java.util.UUID;

@Log4j2
@Value
@AllArgsConstructor
public class SetLimitEvent extends Event {

    @JsonProperty("limit")
    Limit limit;

    public SetLimitEvent(UUID groupId, String exec, @Valid Limit limit) {
        super(groupId, exec, null);
        this.limit = limit;
    }

    @Override
    protected void applyEvent(Group group) throws BadArgumentException, NoAccessException {
        group.setLimit(exec, limit);

        log.trace("\t\t\t\t\tNeues UserLimit: {}", group.getLimit());
    }

    @Override
    public String type() {
        return EventType.SETLIMIT.toString();
    }
}
