package mops.gruppen2.domain.event;

import lombok.Value;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.model.Group;
import mops.gruppen2.domain.model.Title;
import mops.gruppen2.domain.model.User;

/**
 * Ändert nur den Gruppentitel.
 */
@Log4j2
@Value
public class UpdateGroupTitleEvent extends Event {

    Title newGroupTitle;

    public UpdateGroupTitleEvent(Group group, User user, Title newGroupTitle) {
        super(group.getId(), user.getId());
        this.newGroupTitle = newGroupTitle;
    }

    @Override
    protected void applyEvent(Group group) {
        group.setTitle(newGroupTitle);

        log.trace("\t\t\t\t\tNeuer Titel: {}", group.getTitle());
    }

}
