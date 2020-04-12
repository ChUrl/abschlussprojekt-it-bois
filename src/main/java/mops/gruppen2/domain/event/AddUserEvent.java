package mops.gruppen2.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.exception.EventException;
import mops.gruppen2.domain.helper.ValidationHelper;
import mops.gruppen2.domain.model.Group;
import mops.gruppen2.domain.model.Role;
import mops.gruppen2.domain.model.User;

/**
 * Fügt einen einzelnen Nutzer einer Gruppe hinzu.
 */

@Log4j2
@Value
@AllArgsConstructor
public class AddUserEvent extends Event {

    @JsonProperty("givenname")
    String givenname;

    @JsonProperty("familyname")
    String familyname;

    @JsonProperty("email")
    String email;

    public AddUserEvent(Group group, User user) {
        super(group.getGroupid(), user.getUserid());
        givenname = user.getGivenname();
        familyname = user.getFamilyname();
        email = user.getEmail();
    }

    @Override
    protected void applyEvent(Group group) throws EventException {
        ValidationHelper.throwIfMember(group, new User(userid));
        ValidationHelper.throwIfGroupFull(group);

        group.getMembers().put(userid, new User(userid, givenname, familyname, email));
        group.getRoles().put(userid, Role.MEMBER);

        log.trace("\t\t\t\t\tNeue Members: {}", group.getMembers());
        log.trace("\t\t\t\t\tNeue Rollen: {}", group.getRoles());
    }
}
