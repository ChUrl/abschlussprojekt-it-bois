package mops.gruppen2.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.exception.NoAccessException;
import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.domain.model.group.wrapper.Parent;
import mops.gruppen2.infrastructure.GroupCache;

import javax.validation.Valid;
import java.util.UUID;

@Log4j2
@Value
@AllArgsConstructor
public class SetParentEvent extends Event {

    @JsonProperty("parent")
    Parent parent;

    public SetParentEvent(UUID groupId, String exec, @Valid Parent parent) {
        super(groupId, exec, null);
        this.parent = parent;
    }

    @Override
    protected void updateCache(GroupCache cache, Group group) {}

    @Override
    protected void applyEvent(Group group) throws NoAccessException {
        group.setParent(exec, parent);

        log.trace("\t\t\t\t\tNeues Parent: {}", group.getParent());
    }

    @Override
    public String format() {
        return "Veranstaltungszugehörigkeit gesetzt: " + parent.getValue() + ".";
    }

    @Override
    public String type() {
        return EventType.SETPARENT.toString();
    }
}
