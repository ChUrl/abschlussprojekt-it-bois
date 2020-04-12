package mops.gruppen2.domain.event;

import lombok.Value;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.model.Group;
import mops.gruppen2.domain.model.User;

@Log4j2
@Value
public class DeleteGroupEvent extends Event {

    public DeleteGroupEvent(Group group, User user) {
        super(group.getId(), user.getId());
    }

    @Override
    protected void applyEvent(Group group) {
        group.getRoles().clear();
        group.getMembers().clear();
        group.setTitle(null);
        group.setDescription(null);
        group.setType(null);
        group.setParent(null);
        group.setUserLimit(null);

        log.trace("\t\t\t\t\tGelöschte Gruppe: {}", group);
    }
}
