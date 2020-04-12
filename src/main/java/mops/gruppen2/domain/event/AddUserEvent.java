package mops.gruppen2.domain.event;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.exception.EventException;
import mops.gruppen2.domain.helper.ValidationHelper;
import mops.gruppen2.domain.model.Group;
import mops.gruppen2.domain.model.Role;
import mops.gruppen2.domain.model.User;

/**
 * Fügt einen einzelnen Nutzer einer Gruppe hinzu.
 */
@Getter
@ToString
@Log4j2
public class AddUserEvent extends Event {

    private String givenname;
    private String familyname;
    private String email;

    private AddUserEvent() {}

    public AddUserEvent(Group group, User user) {
        super(group.getId(), user.getId());
        givenname = user.getGivenname();
        familyname = user.getFamilyname();
        email = user.getEmail();
    }

    @Override
    protected void applyEvent(Group group) throws EventException {
        ValidationHelper.throwIfMember(group, new User(userId));
        ValidationHelper.throwIfGroupFull(group);

        group.getMembers().put(userId, new User(userId, givenname, familyname, email));
        group.getRoles().put(userId, Role.MEMBER);

        log.trace("\t\t\t\t\tNeue Members: {}", group.getMembers());
        log.trace("\t\t\t\t\tNeue Rollen: {}", group.getRoles());
    }
}
