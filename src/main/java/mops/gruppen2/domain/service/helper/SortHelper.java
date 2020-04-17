package mops.gruppen2.domain.service.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import mops.gruppen2.domain.model.group.Membership;
import mops.gruppen2.domain.model.group.Role;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SortHelper {

    public static List<Membership> sortByMemberRole(List<Membership> memberships) {
        memberships.sort((Membership m1, Membership m2) -> {
            if (m1.getRole() == Role.ADMIN) {
                return -1;
            }
            if (m2.getRole() == Role.ADMIN) {
                return 1;
            }

            return 0;
        });

        return memberships;
    }
}
