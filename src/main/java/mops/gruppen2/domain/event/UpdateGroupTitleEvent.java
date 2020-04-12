package mops.gruppen2.domain.event;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.model.Group;
import mops.gruppen2.domain.model.Title;
import mops.gruppen2.domain.model.User;

/**
 * Ändert nur den Gruppentitel.
 */
@Getter
@ToString
@Log4j2
public class UpdateGroupTitleEvent extends Event {

    private Title newGroupTitle;

    private UpdateGroupTitleEvent() {}

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
