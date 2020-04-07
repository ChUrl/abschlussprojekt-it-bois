package mops.gruppen2.domain.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import mops.gruppen2.domain.Group;
import mops.gruppen2.domain.GroupType;
import mops.gruppen2.domain.Visibility;

import java.util.UUID;

@Getter
@NoArgsConstructor // For Jackson
public class CreateGroupEvent extends Event {

    private Visibility groupVisibility;
    private UUID groupParent;
    private GroupType groupType;
    private long groupUserLimit;

    public CreateGroupEvent(UUID groupId, String userId, UUID parent, GroupType type, Visibility visibility, long userLimit) {
        super(groupId, userId);
        groupParent = parent;
        groupType = type;
        groupVisibility = visibility;
        groupUserLimit = userLimit;
    }

    @Override
    protected void applyEvent(Group group) {
        group.setId(groupId);
        group.setParent(groupParent);
        group.setType(groupType);
        group.setVisibility(groupVisibility);
        group.setUserLimit(groupUserLimit);
    }
}
