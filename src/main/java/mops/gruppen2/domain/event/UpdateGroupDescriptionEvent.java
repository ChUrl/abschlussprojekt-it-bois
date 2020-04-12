package mops.gruppen2.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.model.Description;
import mops.gruppen2.domain.model.Group;
import mops.gruppen2.domain.model.User;

/**
 * Ändert nur die Gruppenbeschreibung.
 */
@Log4j2
@Value
@AllArgsConstructor
public class UpdateGroupDescriptionEvent extends Event {

    @JsonProperty("desc")
    Description description;

    public UpdateGroupDescriptionEvent(Group group, User user, Description description) {
        super(group.getGroupid(), user.getUserid());
        this.description = description;
    }

    @Override
    protected void applyEvent(Group group) {
        group.setDescription(description);

        log.trace("\t\t\t\t\tNeue Beschreibung: {}", group.getDescription());
    }
}
