package mops.gruppen2.domain.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.Group;
import mops.gruppen2.domain.User;

import java.util.UUID;

@Getter
@NoArgsConstructor // For Jackson
@ToString
@Log4j2
public class DeleteGroupEvent extends Event {

    public DeleteGroupEvent(UUID groupId, String userId) {
        super(groupId, userId);
    }

    public DeleteGroupEvent(Group group, User user) {
        super(group.getId(), user.getId());
    }

    @Override
    protected void applyEvent(Group group) {
        group.getRoles().clear();
        group.getMembers().clear();
        group.setTitle(null);
        group.setDescription(null);
        group.setVisibility(null);
        group.setType(null);
        group.setParent(null);
        group.setUserLimit(0L);

        log.trace("\t\t\t\t\tGelöschte Gruppe: {}", group);
    }
}
