package mops.gruppen2.domain.service;

import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.event.Event;
import mops.gruppen2.domain.exception.EventException;
import mops.gruppen2.domain.exception.GroupNotFoundException;
import mops.gruppen2.domain.helper.IdHelper;
import mops.gruppen2.domain.model.Group;
import mops.gruppen2.domain.model.Type;
import mops.gruppen2.domain.model.User;
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
@Log4j2
public class ProjectionService {

    private final EventStoreService eventStoreService;

    public ProjectionService(EventStoreService eventStoreService) {
        this.eventStoreService = eventStoreService;
    }


    // ################################## STATISCHE PROJEKTIONEN #################################


    /**
     * Konstruiert Gruppen aus einer Liste von Events.
     *
     * @param events Liste an Events
     *
     * @return Liste an Projizierten Gruppen
     *
     * @throws EventException Projektionsfehler
     */
    static List<Group> projectGroups(List<Event> events) throws EventException {
        Map<UUID, Group> groupMap = new HashMap<>();

        events.forEach(event -> event.apply(getOrCreateGroup(groupMap, event.getGroupId())));

        return new ArrayList<>(groupMap.values());
    }

    /**
     * Projiziert Events, geht aber davon aus, dass alle zu derselben Gruppe gehören.
     *
     * @param events Eventliste
     *
     * @return Eine projizierte Gruppe
     *
     * @throws EventException Projektionsfehler, z.B. falls Events von verschiedenen Gruppen übergeben werden
     */
    static Group projectSingleGroup(List<Event> events) throws EventException {
        if (events.isEmpty()) {
            throw new GroupNotFoundException(ProjectionService.class.toString());
        }

        Group group = new Group();

        events.forEach(event -> event.apply(group));

        return group;
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


    // ############################### PROJEKTIONEN MIT DATENBANK ################################


    /**
     * Projiziert Gruppen, welche sich seit einer übergebenen eventId geändert haben.
     * Die Gruppen werden dabei vollständig konstruiert.
     *
     * @param status Letzte bekannte eventId
     *
     * @return Liste an Gruppen
     */
    public List<Group> projectNewGroups(long status) {
        List<Event> events = eventStoreService.findChangedGroupEvents(status);

        return projectGroups(events);
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
    public List<Group> projectPublicGroups() throws EventException {
        List<UUID> groupIds = eventStoreService.findExistingGroupIds();
        List<Event> events = eventStoreService.findEventsByGroupAndType(groupIds,
                                                                        "CreateGroupEvent",
                                                                        "UpdateGroupDescriptionEvent",
                                                                        "UpdateGroupTitleEvent",
                                                                        "UpdateUserMaxEvent");

        List<Group> groups = projectGroups(events);

        return groups.stream()
                     .filter(group -> group.getType() != Type.PRIVATE)
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
        List<Event> events = eventStoreService.findEventsByGroupAndType(groupIds,
                                                                        "CreateGroupEvent",
                                                                        "UpdateGroupTitleEvent");

        List<Group> lectures = projectGroups(events);

        return lectures.stream()
                       .filter(group -> group.getType() == Type.LECTURE)
                       .collect(Collectors.toList());
    }

    /**
     * Projiziert Gruppen, in welchen der User aktuell teilnimmt.
     * Die Gruppen enthalten nur Metainformationen: Titel und Beschreibung.
     *
     * @param user Die Id
     *
     * @return Liste aus Gruppen
     */
    @Cacheable("groups")
    public List<Group> projectUserGroups(User user) {
        List<UUID> groupIds = eventStoreService.findExistingUserGroups(user);
        List<Event> groupEvents = eventStoreService.findEventsByGroupAndType(groupIds,
                                                                             "CreateGroupEvent",
                                                                             "UpdateGroupTitleEvent",
                                                                             "UpdateGroupDescriptionEvent");

        return projectGroups(groupEvents);
    }

    /**
     * Gibt die Gruppe zurück, die zu der übergebenen Id passt.
     * Enthält alle verfügbaren Informationen, also auch User (langsam).
     * Gibt eine leere Gruppe zurück, falls die Id leer ist.
     *
     * @param groupId Die Id der gesuchten Gruppe
     *
     * @return Die gesuchte Gruppe
     *
     * @throws GroupNotFoundException Wenn die Gruppe nicht gefunden wird
     */
    public Group projectSingleGroup(UUID groupId) throws GroupNotFoundException {
        if (IdHelper.isEmpty(groupId)) {
            throw new GroupNotFoundException(groupId + ": " + ProjectionService.class);
        }

        try {
            List<Event> events = eventStoreService.findGroupEvents(groupId);
            return projectSingleGroup(events);
        } catch (Exception e) {
            log.error("Gruppe {} wurde nicht gefunden!", groupId.toString(), e);
            throw new GroupNotFoundException(groupId + ": " + ProjectionService.class);
        }
    }

    /**
     * Projiziert eine einzelne Gruppe, welche leer sein darf.
     */
    public Group projectParent(UUID parentId) {
        if (IdHelper.isEmpty(parentId)) {
            return new Group();
        }

        return projectSingleGroup(parentId);
    }

    /**
     * Entfernt alle Gruppen, in welchen ein User teilnimmt, aus einer Gruppenliste.
     *
     * @param groups Gruppenliste, aus der entfernt wird
     * @param user   User, welcher teilnimmt
     */
    void removeUserGroups(List<Group> groups, User user) {
        List<UUID> userGroups = eventStoreService.findExistingUserGroups(user);

        groups.removeIf(group -> userGroups.contains(group.getId()));
    }
}

