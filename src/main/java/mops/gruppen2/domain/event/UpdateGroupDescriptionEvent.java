package mops.gruppen2.domain.event;

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
public class UpdateGroupDescriptionEvent extends Event {

    Description groupDescription;

    public UpdateGroupDescriptionEvent(Group group, User user, Description groupDescription) {
        super(group.getId(), user.getId());
        this.groupDescription = groupDescription;
    }

    @Override
    protected void applyEvent(Group group) {
        group.setDescription(groupDescription);

        log.trace("\t\t\t\t\tNeue Beschreibung: {}", group.getDescription());
    }
}
