package mops.gruppen2.domain.event;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.model.Group;
import mops.gruppen2.domain.model.User;

@Getter
@ToString
@Log4j2
public class DeleteGroupEvent extends Event {

    private DeleteGroupEvent() {}

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
