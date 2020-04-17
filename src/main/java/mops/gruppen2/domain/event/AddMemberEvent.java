package mops.gruppen2.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.exception.GroupFullException;
import mops.gruppen2.domain.exception.IdMismatchException;
import mops.gruppen2.domain.exception.UserAlreadyExistsException;
import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.domain.model.group.User;
import mops.gruppen2.infrastructure.GroupCache;

import java.util.UUID;

/**
 * Fügt einen einzelnen Nutzer einer Gruppe hinzu.
 */
@Log4j2
@Value
@AllArgsConstructor
public class AddMemberEvent extends Event {

    @JsonProperty("user")
    User user;

    public AddMemberEvent(UUID groupId, String exec, String target, User user) throws IdMismatchException {
        super(groupId, exec, target);
        this.user = user;

        if (!target.equals(user.getId())) {
            throw new IdMismatchException("Der User passt nicht zur angegebenen userid.");
        }
    }

    @Override
    protected void updateCache(GroupCache cache, Group group) {
        cache.usersPut(target, group);
    }

    @Override
    protected void applyEvent(Group group) throws UserAlreadyExistsException, GroupFullException {
        group.addMember(target, user);

        log.trace("\t\t\t\t\tNeue Members: {}", group.getMembers());
    }

    @Override
    public String format() {
        return "Benutzer hinzugefügt: " + target + ".";
    }

    @Override
    public String type() {
        return EventType.ADDMEMBER.toString();
    }
}
