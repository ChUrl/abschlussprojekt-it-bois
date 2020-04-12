package mops.gruppen2.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.model.Group;
import mops.gruppen2.domain.model.Type;
import mops.gruppen2.domain.model.User;

import java.util.UUID;

@Log4j2
@Value
@AllArgsConstructor// Value generiert den allArgsConstrucot nur, wenn keiner explizit angegeben ist
public class CreateGroupEvent extends Event {

    @JsonProperty("parent")
    UUID parent;

    @JsonProperty("type")
    Type type;

    public CreateGroupEvent(UUID groupId, User user, UUID parent, Type type) {
        super(groupId, user.getUserid());
        this.parent = parent;
        this.type = type;
    }

    @Override
    protected void applyEvent(Group group) {
        group.setGroupid(groupid);
        group.setParent(parent);
        group.setType(type);

        log.trace("\t\t\t\t\tNeue Gruppe: {}", group.toString());
    }
}
