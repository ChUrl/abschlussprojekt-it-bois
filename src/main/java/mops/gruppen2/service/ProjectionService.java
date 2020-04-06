package mops.gruppen2.service;

import mops.gruppen2.domain.Group;
import mops.gruppen2.domain.GroupType;
import mops.gruppen2.domain.User;
import mops.gruppen2.domain.Visibility;
import mops.gruppen2.domain.event.Event;
import mops.gruppen2.domain.exception.EventException;
import mops.gruppen2.domain.exception.GroupNotFoundException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Liefert verschiedene Projektionen auf Gruppen.
 * Benötigt ausschließlich den EventStoreService.
 */
@Service
public class ProjectionService {

    private final EventStoreService eventStoreService;

    public ProjectionService(EventStoreService eventStoreService) {
        this.eventStoreService = eventStoreService;
    }

    /**
     * Konstruiert Gruppen aus einer Liste von Events.
     *
     * @param events Liste an Events
     *
     * @return Liste an Projizierten Gruppen
     *
     * @throws EventException Projektionsfehler
     */
    public static List<Group> projectEventList(List<Event> events) throws EventException {
        Map<UUID, Group> groupMap = new HashMap<>();

        events.forEach(event -> event.apply(getOrCreateGroup(groupMap, event.getGroupId())));

        return new ArrayList<>(groupMap.values());
    }

    /**
     * Gibt die Gruppe mit der richtigen Id aus der übergebenen Map wieder, existiert diese nicht
     * wird die Gruppe erstellt und der Map hizugefügt.
     *
     * @param groups  Map aus GruppenIds und Gruppen
     * @param groupId Die Id der Gruppe, die zurückgegeben werden soll
     *
     * @return Die gesuchte Gruppe
     */
    private static Group getOrCreateGroup(Map<UUID, Group> groups, UUID groupId) {
        if (!groups.containsKey(groupId)) {
            groups.put(groupId, new Group());
        }

        return groups.get(groupId);
    }

    /**
     * Projiziert öffentliche Gruppen.
     * Die Gruppen enthalten Metainformationen: Titel, Beschreibung und MaxUserAnzahl.
     * Außerdem wird noch beachtet, ob der eingeloggte User bereits in entsprechenden Gruppen mitglied ist.
     *
     * @return Liste von projizierten Gruppen
     *
     * @throws EventException Projektionsfehler
     */
    @Cacheable("groups")
    //TODO: remove userID param
    public List<Group> projectPublicGroups(String userId) throws EventException {
        List<UUID> groupIds = eventStoreService.findExistingGroupIds();
        List<Event> events = eventStoreService.findEventsByGroupsAndTypes(groupIds,
                                                                          "CreateGroupEvent",
                                                                          "UpdateGroupDescriptionEvent",
                                                                          "UpdateGroupTitleEvent",
                                                                          "UpdateUserMaxEvent");

        List<Group> groups = projectEventList(events);

        SearchService.sortByGroupType(groups); //TODO: auslagern?

        return groups.stream()
                     .filter(group -> group.getVisibility() == Visibility.PUBLIC)
                     .filter(group -> !eventStoreService.userInGroup(group.getId(), userId)) //TODO: slow
                     .collect(Collectors.toList());
    }

    /**
     * Projiziert Vorlesungen.
     * Projektionen enthalten nur Metainformationen: Titel.
     *
     * @return Liste von Veranstaltungen
     */
    @Cacheable("groups")
    public List<Group> projectLectures() {
        List<UUID> groupIds = eventStoreService.findExistingGroupIds();
        List<Event> events = eventStoreService.findEventsByGroupsAndTypes(groupIds,
                                                                          "CreateGroupEvent",
                                                                          "UpdateGroupTitleEvent");

        List<Group> lectures = projectEventList(events);

        return lectures.stream()
                       .filter(group -> group.getType() == GroupType.LECTURE)
                       .collect(Collectors.toList());
    }

    @Cacheable("groups")
    public List<Group> projectGroupsByUser(String userId) throws EventException {
        return projectGroupsByUser(new User(userId));
    }

    /**
     * Projiziert Gruppen, in welchen der User aktuell teilnimmt.
     * Die Gruppen enthalten nur Metainformationen: Titel und Beschreibung.
     *
     * @param user Der User
     *
     * @return Liste aus Gruppen
     */
    @Cacheable("groups")
    public List<Group> projectGroupsByUser(User user) {
        List<UUID> groupIds = eventStoreService.findExistingUserGroups(user.getId());
        List<Event> groupEvents = eventStoreService.findEventsByGroupsAndTypes(groupIds,
                                                                               "CreateGroupEvent",
                                                                               "UpdateGroupTitleEvent",
                                                                               "UpdateGroupDescriptionEvent",
                                                                               "DeleteGroupEvent");

        return projectEventList(groupEvents);
    }

    /**
     * Gibt die Gruppe zurück, die zu der übergebenen Id passt.
     *
     * @param groupId Die Id der gesuchten Gruppe
     *
     * @return Die gesuchte Gruppe
     *
     * @throws EventException Wenn die Gruppe nicht gefunden wird
     */
    public Group projectSingleGroupById(UUID groupId) throws GroupNotFoundException {
        try {
            List<Event> events = eventStoreService.getGroupEvents(groupId);
            return projectEventList(events).get(0);
        } catch (IndexOutOfBoundsException e) {
            throw new GroupNotFoundException(ProjectionService.class.toString());
        }
    }
}

