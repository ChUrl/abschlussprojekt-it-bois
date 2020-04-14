package mops.gruppen2.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.exception.NoAccessException;
import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.domain.model.group.wrapper.Title;

import javax.validation.Valid;

/**
 * Ändert nur den Gruppentitel.
 */
@Log4j2
@Value
@AllArgsConstructor
public class SetTitleEvent extends Event {

    @JsonProperty("title")
    Title title;

    public SetTitleEvent(Group group, String exec, @Valid Title title) {
        super(group.getId(), exec, null);
        this.title = title;
    }

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
