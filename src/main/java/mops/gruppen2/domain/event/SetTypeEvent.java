package mops.gruppen2.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.exception.EventException;
import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.domain.model.group.Type;

import javax.validation.Valid;

@Log4j2
@Value
@AllArgsConstructor
public class SetTypeEvent extends Event {

    @JsonProperty("type")
    Type type;

    public SetTypeEvent(Group group, String exec, @Valid Type type) {
        super(group.getId(), exec, null);

        this.type = type;
    }

    @Override
    protected void applyEvent(Group group) throws EventException {
        group.setType(exec, type);
    }

    @Override
    public String getType() {
        return EventType.SETTYPE.toString();
    }
}
