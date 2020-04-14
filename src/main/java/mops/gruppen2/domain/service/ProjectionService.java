package mops.gruppen2.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.event.Event;
import mops.gruppen2.domain.exception.EventException;
import mops.gruppen2.domain.exception.GroupNotFoundException;
import mops.gruppen2.domain.helper.CommonHelper;
import mops.gruppen2.domain.model.group.Group;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Liefert verschiedene Projektionen auf Gruppen.
 * Benötigt ausschließlich den EventStoreService.
 */
@Log4j2
@RequiredArgsConstructor
@Service
public class ProjectionService {

    private final EventStoreService eventStoreService;


    // ################################## STATISCHE PROJEKTIONEN #################################


    /**
     * Projiziert Events, geht aber davon aus, dass alle zu derselben Gruppe gehören.
     *
     * @param events Eventliste
     *
     * @return Eine projizierte Gruppe
     *
     * @throws EventException Projektionsfehler, z.B. falls Events von verschiedenen Gruppen übergeben werden
     */
    private static Group projectGroupByEvents(List<Event> events) throws EventException {
        if (events.isEmpty()) {
            throw new GroupNotFoundException(ProjectionService.class.toString());
        }

        Group group = new Group();

        events.forEach(event -> event.apply(group));

        return group;
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
    private static List<Group> projectGroupsByEvents(List<Event> events) throws EventException {
        Map<UUID, Group> groupMap = new HashMap<>();

        events.forEach(event -> event.apply(getOrCreateGroup(groupMap, event.getGroupid())));

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


    // ############################### PROJEKTIONEN MIT DATENBANK ################################


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
    public Group projectGroupById(UUID groupId) throws GroupNotFoundException {
        try {
            List<Event> events = eventStoreService.findGroupEvents(groupId);
            return projectGroupByEvents(events);
        } catch (Exception e) {
            log.error("Gruppe {} wurde nicht gefunden!", groupId.toString(), e);
            throw new GroupNotFoundException(groupId + ": " + ProjectionService.class);
        }
    }

    public Group projectParent(UUID parent) {
        if (CommonHelper.uuidIsEmpty(parent)) {
            return new Group();
        }

        return projectGroupById(parent);
    }

    public List<Group> projectGroupsByIds(List<UUID> groupids) {
        List<Event> events = eventStoreService.findGroupEvents(groupids);

        return projectGroupsByEvents(events);
    }

    /**
     * Projiziert Gruppen, welche sich seit einer übergebenen eventId geändert haben.
     * Die Gruppen werden dabei vollständig konstruiert.
     *
     * @param status Letzte bekannte eventId
     *
     * @return Liste an Gruppen
     */
    public List<Group> projectChangedGroups(long status) {
        List<UUID> changedids = eventStoreService.findChangedGroups(status);

        return projectGroupsByIds(changedids);
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
    public List<Group> projectPublicGroups() throws EventException {
        List<UUID> groupIds = eventStoreService.findPublicGroupIds();

        if (groupIds.isEmpty()) {
            return Collections.emptyList();
        }

        return projectGroupsByIds(groupIds);
    }

    /**
     * Projiziert Vorlesungen.
     * Projektionen enthalten nur Metainformationen: Titel.
     *
     * @return Liste von Veranstaltungen
     */
    @Cacheable("groups")
    public List<Group> projectLectures() {
        List<UUID> groupIds = eventStoreService.findLectureGroupIds();

        if (groupIds.isEmpty()) {
            return Collections.emptyList();
        }

        return projectGroupsByIds(groupIds);
    }

    /**
     * Projiziert Gruppen, in welchen der User aktuell teilnimmt.
     * Die Gruppen enthalten nur Metainformationen: Titel und Beschreibung.
     *
     * @param userid Die Id
     *
     * @return Liste aus Gruppen
     */
    @Cacheable("groups")
    public List<Group> projectUserGroups(String userid) {
        List<UUID> groupIds = eventStoreService.findExistingUserGroups(userid);

        if (groupIds.isEmpty()) {
            return Collections.emptyList();
        }

        return projectGroupsByIds(groupIds);
    }

    /**
     * Entfernt alle Gruppen, in welchen ein User teilnimmt, aus einer Gruppenliste.
     *
     * @param groups Gruppenliste, aus der entfernt wird
     * @param userid User, welcher teilnimmt
     */
    void removeUserGroups(List<Group> groups, String userid) {
        List<UUID> userGroups = eventStoreService.findExistingUserGroups(userid);

        groups.removeIf(group -> userGroups.contains(group.getId()));
    }

    public Group projectGroupByLink(String link) {
        return projectGroupById(eventStoreService.findGroupByLink(link));
    }
}

