package mops.gruppen2.infrastructure.api;

import lombok.Value;
import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.domain.model.group.Type;
import mops.gruppen2.domain.model.group.User;

import java.util.List;
import java.util.UUID;

@Value
public class GroupWrapper {

    UUID groupid;
    Type type;
    UUID parent;
    String title;
    String description;
    List<User> admins;
    List<User> regulars;

    public GroupWrapper(Group group) {
        groupid = group.getId();
        type = group.getType();
        parent = group.getParent();
        title = group.getTitle();
        description = group.getDescription();
        admins = group.getAdmins();
        regulars = group.getRegulars();
    }
}
