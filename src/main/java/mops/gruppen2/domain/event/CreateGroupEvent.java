package mops.gruppen2.domain.event;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.model.Group;
import mops.gruppen2.domain.model.Type;

import java.util.UUID;

@Getter
@ToString
@Log4j2
public class CreateGroupEvent extends Event {

    private UUID groupParent;
    private Type type;

    private CreateGroupEvent() {}

    public CreateGroupEvent(UUID groupId, String userId, UUID parent, Type type) {
        super(groupId, userId);
        groupParent = parent;
        this.type = type;
    }

    @Override
    protected void applyEvent(Group group) {
        group.setId(groupId);
        group.setParent(groupParent);
        group.setType(type);

        log.trace("\t\t\t\t\tNeue Gruppe: {}", group.toString());
    }
}
