package mops.gruppen2.domain.event;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.model.Description;
import mops.gruppen2.domain.model.Group;
import mops.gruppen2.domain.model.User;

/**
 * Ändert nur die Gruppenbeschreibung.
 */
@Getter
@ToString
@Log4j2
public class UpdateGroupDescriptionEvent extends Event {

    private Description groupDescription;

    private UpdateGroupDescriptionEvent() {}

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
