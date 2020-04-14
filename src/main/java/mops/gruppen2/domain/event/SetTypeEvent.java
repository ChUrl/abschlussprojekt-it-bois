package mops.gruppen2.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.exception.EventException;
import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.domain.model.group.Type;

import javax.validation.Valid;
import java.util.UUID;

@Log4j2
@Value
@AllArgsConstructor
public class SetTypeEvent extends Event {

    @JsonProperty("type")
    Type type;

    public SetTypeEvent(UUID groupId, String exec, @Valid Type type) {
        super(groupId, exec, null);

        this.type = type;
    }

    @Override
    protected void applyEvent(Group group) throws EventException {
        group.setType(exec, type);
    }

    @Override
    public String type() {
        return EventType.SETTYPE.toString();
    }
}
