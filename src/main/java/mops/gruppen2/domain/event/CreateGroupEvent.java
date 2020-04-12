package mops.gruppen2.domain.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.Group;
import mops.gruppen2.domain.GroupType;

import java.util.UUID;

@Getter
@NoArgsConstructor // For Jackson
@ToString
@Log4j2
public class CreateGroupEvent extends Event {

    private UUID groupParent;
    private GroupType groupType;

    public CreateGroupEvent(UUID groupId, String userId, UUID parent, GroupType type) {
        super(groupId, userId);
        groupParent = parent;
        groupType = type;
    }

    @Override
    protected void applyEvent(Group group) {
        group.setId(groupId);
        group.setParent(groupParent);
        group.setType(groupType);

        log.trace("\t\t\t\t\tNeue Gruppe: {}", group.toString());
    }
}
