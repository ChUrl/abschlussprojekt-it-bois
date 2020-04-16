package mops.gruppen2.domain.model.group;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SortHelper {

    /**
     * Sortiert die übergebene Liste an Gruppen, sodass Veranstaltungen am Anfang der Liste sind.
     *
     * @param groups Die Liste von Gruppen die sortiert werden soll
     */
    public static void sortByGroupType(List<Group> groups) {
        groups.sort((Group g1, Group g2) -> {
            if (g1.getType() == Type.LECTURE) {
                return -1;
            }
            if (g2.getType() == Type.LECTURE) {
                return 1;
            }

            return 0;
        });
    }

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
