package mops.gruppen2.service;

import mops.gruppen2.domain.Account;
import mops.gruppen2.domain.Group;
import mops.gruppen2.domain.GroupType;
import mops.gruppen2.domain.User;
import mops.gruppen2.domain.Visibility;
import mops.gruppen2.domain.dto.EventDTO;
import mops.gruppen2.domain.event.Event;
import mops.gruppen2.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Behandelt Aufgaben, welche sich auf eine Gruppe beziehen
 */
@Service
public class GroupService {

    private final EventStoreService eventStoreService;
    private final EventRepository eventRepository;

    public GroupService(EventStoreService eventStoreService, EventRepository eventRepository) {
        this.eventStoreService = eventStoreService;
        this.eventRepository = eventRepository;
    }

    static User getVeteranMember(Account account, Group group) {
        List<User> members = group.getMembers();
        String newAdminId;
        if (members.get(0).getId().equals(account.getName())) {
            newAdminId = members.get(1).getId();
        } else {
            newAdminId = members.get(0).getId();
        }
        return new User(newAdminId, "", "", "");
    }

    /**
     * Wenn die maximale Useranzahl unendlich ist, wird das Maximum auf 100000 gesetzt. Praktisch gibt es also Maximla 100000
     * Nutzer pro Gruppe.
     *
     * @param isMaximumInfinite Gibt an ob es unendlich viele User geben soll
     * @param userMaximum       Das Maximum an Usern, falls es eins gibt
     *
     * @return Maximum an Usern
     */
    static Long checkInfiniteUsers(Boolean isMaximumInfinite, Long userMaximum) {
        isMaximumInfinite = isMaximumInfinite != null;

        if (isMaximumInfinite) {
            userMaximum = 100_000L;
        }

        return userMaximum;
    }

    static void removeOldUsersFromNewUsers(List<User> oldUsers, List<User> newUsers) {
        for (User oldUser : oldUsers) {
            newUsers.remove(oldUser);
        }
    }

    static Long adjustUserMaximum(Long newUsers, Long oldUsers, Long maxUsers) {
        if (oldUsers + newUsers > maxUsers) {
            maxUsers = oldUsers + newUsers;
        }
        return maxUsers;
    }

    static Visibility setGroupVisibility(Boolean isVisibilityPrivate) {
        isVisibilityPrivate = isVisibilityPrivate != null;

        if (isVisibilityPrivate) {
            return Visibility.PRIVATE;
        } else {
            return Visibility.PUBLIC;
        }
    }

    static GroupType setGroupType(Boolean isLecture) {
        isLecture = isLecture != null;
        if (isLecture) {
            return GroupType.LECTURE;
        } else {
            return GroupType.SIMPLE;
        }
    }

    static boolean idIsEmpty(UUID id) {
        if (id == null) {
            return true;
        }

        return "00000000-0000-0000-0000-000000000000".equals(id.toString());
    }

    /**
     * Sucht in der DB alle Zeilen raus welche eine der Gruppen_ids hat.
     * Wandelt die Zeilen in Events um und gibt davon eine Liste zurück.
     *
     * @param groupIds Liste an IDs
     *
     * @return Liste an Events
     */
    //TODO: Das vielleicht in den EventRepoService?
    public List<Event> getGroupEvents(List<UUID> groupIds) {
        List<EventDTO> eventDTOS = new ArrayList<>();
        for (UUID groupId : groupIds) {
            eventDTOS.addAll(eventRepository.findEventDTOByGroupId(groupId.toString()));
        }
        return eventStoreService.getEventsFromDTOs(eventDTOS);
    }

}
