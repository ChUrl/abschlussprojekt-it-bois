package mops.gruppen2.domain.helper;

import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.exception.BadParameterException;
import mops.gruppen2.domain.exception.GroupFullException;
import mops.gruppen2.domain.exception.NoAccessException;
import mops.gruppen2.domain.exception.NoAdminAfterActionException;
import mops.gruppen2.domain.exception.UserAlreadyExistsException;
import mops.gruppen2.domain.exception.UserNotFoundException;
import mops.gruppen2.domain.model.Group;
import mops.gruppen2.domain.model.Type;
import mops.gruppen2.domain.model.User;
import mops.gruppen2.web.form.CreateForm;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;

import static mops.gruppen2.domain.model.Role.ADMIN;

@Log4j2
public final class ValidationHelper {

    private ValidationHelper() {}


    // ######################################## CHECK ############################################


    /**
     * Überprüft, ob ein User in einer Gruppe teilnimmt.
     */
    public static boolean checkIfMember(Group group, User user) {
        return group.getMembers().containsKey(user.getId());
    }

    public static boolean checkIfLastMember(User user, Group group) {
        return checkIfMember(group, user) && group.getMembers().size() == 1;
    }

    /**
     * Überprüft, ob eine Gruppe voll ist.
     */
    public static boolean checkIfGroupFull(Group group) {
        return group.getMembers().size() >= group.getUserLimit().getUserLimit();
    }

    /**
     * Überprüft, ob eine Gruppe leer ist.
     */
    public static boolean checkIfGroupEmpty(Group group) {
        return group.getMembers().isEmpty();
    }

    /**
     * Überprüft, ob ein User in einer Gruppe Admin ist.
     */
    public static boolean checkIfAdmin(Group group, User user) {
        if (checkIfMember(group, user)) {
            return group.getRoles().get(user.getId()) == ADMIN;
        }
        return false;
    }

    public static boolean checkIfLastAdmin(User user, Group group) {
        return checkIfAdmin(group, user) && group.getRoles().values().stream()
                                                 .filter(role -> role == ADMIN)
                                                 .count() == 1;
    }


    // ######################################## THROW ############################################


    public static void throwIfMember(Group group, User user) {
        if (checkIfMember(group, user)) {
            log.error("Benutzer {} ist schon in Gruppe {}", user, group);
            throw new UserAlreadyExistsException(user.toString());
        }
    }

    public static void throwIfNoMember(Group group, User user) {
        if (!checkIfMember(group, user)) {
            log.error("Benutzer {} ist nicht in Gruppe {}!", user, group);
            throw new UserNotFoundException(user.toString());
        }
    }

    public static void throwIfNoAdmin(Group group, User user) {
        if (!checkIfAdmin(group, user)) {
            log.error("User {} ist kein Admin in Gruppe {}!", user, group);
            throw new NoAccessException(group.toString());
        }
    }

    /**
     * Schmeißt keine Exception, wenn der User der letzte User ist.
     */
    public static void throwIfLastAdmin(User user, Group group) {
        if (!checkIfLastMember(user, group) && checkIfLastAdmin(user, group)) {
            throw new NoAdminAfterActionException("Du bist letzter Admin!");
        }
    }

    public static void throwIfGroupFull(Group group) {
        if (checkIfGroupFull(group)) {
            log.error("Die Gruppe {} ist voll!", group);
            throw new GroupFullException(group.toString());
        }
    }


    // ##################################### VALIDATE FIELDS #####################################

    public static void validateCreateForm(KeycloakAuthenticationToken token, CreateForm form) {
        if (!token.getAccount().getRoles().contains("orga")
            && form.getType() == Type.LECTURE) {
            throw new BadParameterException("Eine Veranstaltung kann nur von ORGA erstellt werden.");
        }
    }
}
