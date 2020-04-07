package mops.gruppen2.service;

import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.Account;
import mops.gruppen2.domain.Group;
import mops.gruppen2.domain.Role;
import mops.gruppen2.domain.User;
import mops.gruppen2.domain.Visibility;
import mops.gruppen2.domain.exception.BadParameterException;
import mops.gruppen2.domain.exception.GroupFullException;
import mops.gruppen2.domain.exception.GroupNotFoundException;
import mops.gruppen2.domain.exception.NoAccessException;
import mops.gruppen2.domain.exception.NoAdminAfterActionException;
import mops.gruppen2.domain.exception.UserAlreadyExistsException;
import mops.gruppen2.domain.exception.UserNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static mops.gruppen2.domain.Role.ADMIN;

@Service
@Log4j2
public class ValidationService {

    private final SearchService searchService;
    private final ProjectionService projectionService;

    public ValidationService(SearchService searchService, ProjectionService projectionService) {
        this.searchService = searchService;
        this.projectionService = projectionService;
    }

    //TODO: make static or change return + assignment
    public List<Group> checkSearch(String search, List<Group> groups, Account account) {
        if (search != null) {
            groups = searchService.searchPublicGroups(search, account.getName());
        }
        return groups;
    }

    //TODO: what the fuck
    public void throwIfGroupNotExisting(String title) {
        if (title == null) {
            throw new GroupNotFoundException("@details");
        }
    }

    public void throwIfUserAlreadyInGroup(Group group, User user) {
        if (checkIfUserInGroup(group, user)) {
            throw new UserAlreadyExistsException("@details");
        }
    }

    void throwIfNotInGroup(Group group, User user) {
        if (!checkIfUserInGroup(group, user)) {
            throw new UserNotFoundException(getClass().toString());
        }
    }

    public boolean checkIfUserInGroup(Group group, User user) {
        return group.getMembers().contains(user);
    }

    public void throwIfGroupFull(Group group) {
        if (group.getUserLimit() < group.getMembers().size() + 1) {
            throw new GroupFullException("Du kannst der Gruppe daher leider nicht beitreten.");
        }
    }

    //TODO: necessary?
    boolean checkIfGroupEmpty(UUID groupId) {
        return projectionService.projectSingleGroup(groupId).getMembers().isEmpty();
    }

    boolean checkIfGroupEmpty(Group group) {
        return group.getMembers().isEmpty();
    }

    public void throwIfNoAdmin(Group group, User user) {
        throwIfNoAccessToPrivate(group, user);
        if (group.getRoles().get(user.getId()) != ADMIN) {
            throw new NoAccessException("");
        }
    }

    public void throwIfNoAccessToPrivate(Group group, User user) {
        if (!checkIfUserInGroup(group, user) && group.getVisibility() == Visibility.PRIVATE) {
            throw new NoAccessException("");
        }
    }

    public boolean checkIfAdmin(Group group, User user) {
        if (checkIfUserInGroup(group, user)) {
            return group.getRoles().get(user.getId()) == ADMIN;
        }
        return false;
    }

    /**
     * Schmeißt keine Exception, wenn der User der letzte User ist.
     */
    void throwIfLastAdmin(User user, Group group) {
        if (!checkIfLastMember(user, group) && checkIfLastAdmin(user, group)) {
            throw new NoAdminAfterActionException("Du bist letzter Admin!");
        }
    }

    boolean checkIfLastAdmin(User user, Group group) {
        for (Map.Entry<String, Role> entry : group.getRoles().entrySet()) {
            if (entry.getValue() == ADMIN && !(entry.getKey().equals(user.getId()))) {
                return false;
            }
        }
        return true;
    }

    boolean checkIfLastMember(User user, Group group) {
        return group.getMembers().contains(user) && group.getMembers().size() == 1;
    }

    /**
     * Überprüft, ob alle Felder richtig gesetzt sind.
     *
     * @param description Die Beschreibung der Gruppe
     * @param title       Der Titel der Gruppe
     * @param userLimit   Das user Limit der Gruppe
     */
    public void checkFields(String title, String description, Long userLimit, Boolean maxInfiniteUsers) {
        if (description == null || description.trim().isEmpty()) {
            throw new BadParameterException("Die Beschreibung wurde nicht korrekt angegeben");
        }

        if (title == null || title.trim().isEmpty()) {
            throw new BadParameterException("Der Titel wurde nicht korrekt angegeben");
        }

        if (userLimit == null && maxInfiniteUsers == null) {
            throw new BadParameterException("Teilnehmeranzahl wurde nicht korrekt angegeben");
        }

        if (userLimit != null && (userLimit < 1 || userLimit > 100_000L)) {
            throw new BadParameterException("Teilnehmeranzahl wurde nicht korrekt angegeben");
        }
    }

    public void checkFields(String title, String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new BadParameterException("Die Beschreibung wurde nicht korrekt angegeben");
        }

        if (title == null || title.trim().isEmpty()) {
            throw new BadParameterException("Der Titel wurde nicht korrekt angegeben");
        }
    }

    public void throwIfNewUserLimitIsValid(Long newUserLimit, Group group) {
        if (newUserLimit == null) {
            throw new BadParameterException("Es wurde keine neue maximale Teilnehmeranzahl angegeben!");
        }

        if (newUserLimit < 1 || newUserLimit > 100_000L) {
            throw new BadParameterException("Die neue maximale Teilnehmeranzahl wurde nicht korrekt angegeben!");
        }

        if (group.getMembers().size() > newUserLimit) {
            throw new BadParameterException("Die neue maximale Teilnehmeranzahl ist kleiner als die aktuelle Teilnehmeranzahl!");
        }
    }
}
