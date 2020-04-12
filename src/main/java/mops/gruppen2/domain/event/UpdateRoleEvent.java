package mops.gruppen2.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.exception.UserNotFoundException;
import mops.gruppen2.domain.helper.ValidationHelper;
import mops.gruppen2.domain.model.Group;
import mops.gruppen2.domain.model.Role;
import mops.gruppen2.domain.model.User;

/**
 * Aktualisiert die Gruppenrolle eines Teilnehmers.
 */
@Log4j2
@Value
@AllArgsConstructor
public class UpdateRoleEvent extends Event {

    @JsonProperty("role")
    Role role;

    public UpdateRoleEvent(Group group, User user, Role tole) {
        super(group.getGroupid(), user.getUserid());
        role = tole;
    }

    @Override
    protected void applyEvent(Group group) throws UserNotFoundException {
        ValidationHelper.throwIfNoMember(group, new User(userid));

        group.getRoles().put(userid, role);

        log.trace("\t\t\t\t\tNeue Rollen: {}", group.getRoles());
    }

}
