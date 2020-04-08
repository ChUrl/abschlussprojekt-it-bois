package mops.gruppen2.domain.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.Group;
import mops.gruppen2.domain.User;
import mops.gruppen2.domain.exception.BadParameterException;

import java.util.UUID;

/**
 * Ändert nur den Gruppentitel.
 */
@Getter
@NoArgsConstructor // For Jackson
@ToString
@Log4j2
public class UpdateGroupTitleEvent extends Event {

    private String newGroupTitle;

    public UpdateGroupTitleEvent(UUID groupId, String userId, String newGroupTitle) {
        super(groupId, userId);
        this.newGroupTitle = newGroupTitle.trim();
    }

    public UpdateGroupTitleEvent(Group group, User user, String newGroupTitle) {
        super(group.getId(), user.getId());
        this.newGroupTitle = newGroupTitle.trim();
    }

    @Override
    protected void applyEvent(Group group) {
        if (newGroupTitle.isEmpty()) {
            throw new BadParameterException("Der Titel ist leer.");
        }

        group.setTitle(newGroupTitle);

        log.trace("\t\t\t\t\tNeuer Titel: {}", group.getTitle());
    }

}
