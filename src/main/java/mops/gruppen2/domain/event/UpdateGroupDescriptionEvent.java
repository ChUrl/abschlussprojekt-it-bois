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
 * Ändert nur die Gruppenbeschreibung.
 */
@Getter
@NoArgsConstructor // For Jackson
@ToString
@Log4j2
public class UpdateGroupDescriptionEvent extends Event {

    private String newGroupDescription;

    public UpdateGroupDescriptionEvent(UUID groupId, String userId, String newGroupDescription) {
        super(groupId, userId);
        this.newGroupDescription = newGroupDescription.trim();
    }

    public UpdateGroupDescriptionEvent(Group group, User user, String newGroupDescription) {
        super(group.getId(), user.getId());
        this.newGroupDescription = newGroupDescription.trim();
    }

    @Override
    protected void applyEvent(Group group) {
        if (newGroupDescription.isEmpty()) {
            throw new BadParameterException("Die Beschreibung ist leer.");
        }

        group.setDescription(newGroupDescription);

        log.trace("\t\t\t\t\tNeue Beschreibung: {}", group.getDescription());
    }
}
