package mops.gruppen2.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.exception.NoAccessException;
import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.domain.model.group.wrapper.Description;
import mops.gruppen2.infrastructure.GroupCache;

import javax.validation.Valid;
import java.util.UUID;

/**
 * Ändert nur die Gruppenbeschreibung.
 */
@Log4j2
@Value
@AllArgsConstructor
public class SetDescriptionEvent extends Event {

    @JsonProperty("desc")
    Description description;

    public SetDescriptionEvent(UUID groupId, String exec, @Valid Description description) {
        super(groupId, exec, null);
        this.description = description;
    }

    @Override
    protected void updateCache(GroupCache cache, Group group) {}

    @Override
    protected void applyEvent(Group group) throws NoAccessException {
        group.setDescription(exec, description);

        log.trace("\t\t\t\t\tNeue Beschreibung: {}", group.getDescription());
    }

    @Override
    public String format() {
        return "Beschreibung gesetzt: " + description + ".";
    }

    @Override
    public String type() {
        return EventType.SETDESCRIPTION.toString();
    }
}
