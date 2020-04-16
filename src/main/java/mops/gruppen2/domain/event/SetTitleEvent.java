package mops.gruppen2.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.exception.NoAccessException;
import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.domain.model.group.wrapper.Title;
import mops.gruppen2.infrastructure.GroupCache;

import javax.validation.Valid;
import java.util.UUID;

/**
 * Ändert nur den Gruppentitel.
 */
@Log4j2
@Value
@AllArgsConstructor
public class SetTitleEvent extends Event {

    @JsonProperty("title")
    Title title;

    public SetTitleEvent(UUID groupId, String exec, @Valid Title title) {
        super(groupId, exec, null);
        this.title = title;
    }

    @Override
    protected void updateCache(GroupCache cache, Group group) {}

    @Override
    protected void applyEvent(Group group) throws NoAccessException {
        group.setTitle(exec, title);

        log.trace("\t\t\t\t\tNeuer Titel: {}", group.getTitle());
    }

    @Override
    public String type() {
        return EventType.SETTITLE.toString();
    }

}
