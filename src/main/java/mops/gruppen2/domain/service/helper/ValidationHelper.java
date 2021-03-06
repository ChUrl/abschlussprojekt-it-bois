package mops.gruppen2.domain.service.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.exception.BadArgumentException;
import mops.gruppen2.domain.exception.GroupFullException;
import mops.gruppen2.domain.exception.LastAdminException;
import mops.gruppen2.domain.exception.NoAccessException;
import mops.gruppen2.domain.exception.UserExistsException;
import mops.gruppen2.domain.exception.UserNotFoundException;
import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.domain.model.group.Type;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ValidationHelper {

    public static boolean checkIfLastMember(Group group, String userid) {
        return group.isMember(userid) && group.size() == 1;
    }

    /**
     * Überprüft, ob ein User in einer Gruppe Admin ist.
     */
    public static boolean checkIfAdmin(Group group, String userid) {
        if (group.isMember(userid)) {
            return group.isAdmin(userid);
        }
        return false;
    }

    public static boolean checkIfLastAdmin(Group group, String userid) {
        return checkIfAdmin(group, userid) && group.getAdmins().size() == 1;
    }


    // ######################################## THROW ############################################


    public static void throwIfMember(Group group, String userid) throws UserExistsException {
        if (group.isMember(userid)) {
            log.error("Benutzer {} ist schon in Gruppe {}", userid, group);
            throw new UserExistsException(userid);
        }
    }

    public static void throwIfNoMember(Group group, String userid) throws UserNotFoundException {
        if (!group.isMember(userid)) {
            log.error("Benutzer {} ist nicht in Gruppe {}!", userid, group);
            throw new UserNotFoundException(userid);
        }
    }

    public static void throwIfNoAdmin(Group group, String userid) throws NoAccessException {
        if (!checkIfAdmin(group, userid)) {
            log.error("User {} ist kein Admin in Gruppe {}!", userid, group);
            throw new NoAccessException(group.getId().toString());
        }
    }

    /**
     * Schmeißt keine Exception, wenn der User der letzte User ist.
     */
    public static void throwIfLastAdmin(Group group, String userid) throws LastAdminException {
        if (!checkIfLastMember(group, userid) && checkIfLastAdmin(group, userid)) {
            throw new LastAdminException("Du bist letzter Admin!");
        }
    }

    public static void throwIfGroupFull(Group group) throws GroupFullException {
        if (group.isFull()) {
            log.error("Die Gruppe {} ist voll!", group);
            throw new GroupFullException(group.getId().toString());
        }
    }

    public static void validateCreateForm(KeycloakAuthenticationToken token, Type type) {
        if (!token.getAccount().getRoles().contains("orga") && type == Type.LECTURE) {
            throw new BadArgumentException("Nur Orga kann Veranstaltungen erstellen.");
        }
    }
}
