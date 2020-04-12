package mops.gruppen2.domain.service;

import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.Group;
import mops.gruppen2.domain.GroupType;
import mops.gruppen2.domain.User;
import mops.gruppen2.domain.exception.BadParameterException;
import mops.gruppen2.domain.exception.GroupFullException;
import mops.gruppen2.domain.exception.NoAccessException;
import mops.gruppen2.domain.exception.NoAdminAfterActionException;
import mops.gruppen2.domain.exception.UserAlreadyExistsException;
import mops.gruppen2.domain.exception.UserNotFoundException;
import mops.gruppen2.web.form.CreateForm;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.stereotype.Service;

import static mops.gruppen2.domain.Role.ADMIN;

@Service
@Log4j2
public final class ValidationService {

    private ValidationService() {}


    // ######################################## CHECK ############################################


    /**
     * Überprüft, ob ein User in einer Gruppe teilnimmt.
     */
    public static boolean checkIfMember(Group group, User user) {
        return group.getMembers().contains(user);
    }

    public static boolean checkIfLastMember(User user, Group group) {
        return checkIfMember(group, user) && group.getMembers().size() == 1;
    }

    /**
     * Überprüft, ob eine Gruppe voll ist.
     */
    public static boolean checkIfGroupFull(Group group) {
        return group.getMembers().size() >= group.getUserLimit();
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


    //TODO: max title length?
    public static void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            log.error("Der Titel {} ist fehlerhaft!", title);
            throw new BadParameterException("Der Titel darf nicht leer sein!");
        }
    }

    //TODO: max description length?
    public static void validateDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            log.error("Die Beschreibung {} ist fehlerhaft!", description);
            throw new BadParameterException("Die Beschreibung darf nicht leer sein!");
        }
    }

    public static void validateUserLimit(long userLimit, Group group) {
        if (userLimit < 1) {
            throw new BadParameterException("Das Userlimit muss größer als 1 sein!");
        }

        if (userLimit < group.getMembers().size()) {
            throw new BadParameterException("Das Userlimit kann nicht unter der momentanen Mitgliederanzahl sein!");
        }
    }

    public static void validateCreateForm(KeycloakAuthenticationToken token, CreateForm form) {
        if (!token.getAccount().getRoles().contains("orga")
            && form.getType() == GroupType.LECTURE) {
            throw new BadParameterException("Eine Veranstaltung kann nur von ORGA erstellt werden.");
        }
    }
}
