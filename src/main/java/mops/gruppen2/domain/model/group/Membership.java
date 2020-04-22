package mops.gruppen2.domain.model.group;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Membership {

    User user;
    Role role;

    // LocalDateTime age;

    @Override
    public String toString() {
        return user.format() + ": " + role;
    }

    public Membership setRole(Role role) {
        return new Membership(user, role);
    }
}
