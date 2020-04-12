package mops.gruppen2.domain.event;

import lombok.Value;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.model.Group;
import mops.gruppen2.domain.model.Type;

import java.util.UUID;

@Log4j2
@Value
public class CreateGroupEvent extends Event {

    UUID groupParent;
    Type type;

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
