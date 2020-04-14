package mops.gruppen2.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.aspect.annotation.TraceMethodCalls;
import mops.gruppen2.domain.event.AddMemberEvent;
import mops.gruppen2.domain.event.CreateGroupEvent;
import mops.gruppen2.domain.event.Event;
import mops.gruppen2.domain.event.EventType;
import mops.gruppen2.domain.event.SetTypeEvent;
import mops.gruppen2.domain.exception.BadPayloadException;
import mops.gruppen2.domain.exception.InvalidInviteException;
import mops.gruppen2.domain.helper.CommonHelper;
import mops.gruppen2.domain.helper.JsonHelper;
import mops.gruppen2.domain.model.group.Type;
import mops.gruppen2.persistance.EventRepository;
import mops.gruppen2.persistance.dto.EventDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static mops.gruppen2.domain.event.EventType.CREATEGROUP;
import static mops.gruppen2.domain.event.EventType.DESTROYGROUP;
import static mops.gruppen2.domain.event.EventType.SETLINK;
import static mops.gruppen2.domain.event.EventType.SETTYPE;
import static mops.gruppen2.domain.helper.CommonHelper.eventTypesToString;
import static mops.gruppen2.domain.helper.CommonHelper.uuidsToString;

@Log4j2
@RequiredArgsConstructor
@Service
@TraceMethodCalls
public class EventStoreService {

    private final EventRepository eventStore;


    //########################################### SAVE ###########################################


    /**
     * Erzeugt ein DTO aus einem Event und speicher es.
     *
     * @param event Event, welches gespeichert wird
     */
    public void saveEvent(Event event) {
        eventStore.save(getDTOFromEvent(event));
    }

    public void saveAll(Event... events) {
        for (Event event : events) {
            eventStore.save(getDTOFromEvent(event));
        }
    }

    /**
     * Speichert alle Events aus der übergebenen Liste in der DB.
     *
     * @param events Liste an Events die gespeichert werden soll
     */
    @SafeVarargs
    public final void saveAll(List<Event>... events) {
        for (List<Event> eventlist : events) {
            for (Event event : eventlist) {
                eventStore.save(getDTOFromEvent(event));
            }
        }
    }


    //########################################### DTOs ###########################################


    private static List<EventDTO> getDTOsFromEvents(List<Event> events) {
        return events.stream()
                     .map(EventStoreService::getDTOFromEvent)
                     .collect(Collectors.toList());
    }

    /**
     * Erzeugt aus einem Event Objekt ein EventDTO Objekt.
     *
     * @param event Event, welches in DTO übersetzt wird
     *
     * @return EventDTO (Neues DTO)
     */
    private static EventDTO getDTOFromEvent(Event event) {
        try {
            String payload = JsonHelper.serializeEvent(event);
            return new EventDTO(null,
                                event.getGroupid().toString(),
                                event.getVersion(),
                                event.getExec(),
                                event.getTarget(),
                                event.type(),
                                payload);
        } catch (JsonProcessingException e) {
            log.error("Event ({}) konnte nicht serialisiert werden!", event, e);
            throw new BadPayloadException(EventStoreService.class.toString());
        }
    }

    /**
     * Erzeugt aus einer Liste von eventDTOs eine Liste von Events.
     *
     * @param eventDTOS Liste von DTOs
     *
     * @return Liste von Events
     */
    private static List<Event> getEventsFromDTOs(List<EventDTO> eventDTOS) {
        return eventDTOS.stream()
                        .map(EventStoreService::getEventFromDTO)
                        .collect(Collectors.toList());
    }

    private static Event getEventFromDTO(EventDTO dto) {
        try {
            return JsonHelper.deserializeEvent(dto.getEvent_payload());
        } catch (JsonProcessingException e) {
            log.error("Payload {} konnte nicht deserialisiert werden!", dto.getEvent_payload(), e);
            throw new BadPayloadException(EventStoreService.class.toString());
        }
    }


    // ######################################## QUERIES ##########################################


    List<Event> findGroupEvents(UUID groupId) {
        return getEventsFromDTOs(eventStore.findEventDTOsByGroup(Collections.singletonList(groupId.toString())));
    }

    /**
     * Sucht alle Events, welche zu einer der übergebenen Gruppen gehören.
     *
     * @param groupIds Liste an IDs
     *
     * @return Liste an Events
     */
    List<Event> findGroupEvents(List<UUID> groupIds) {
        List<EventDTO> eventDTOS = new ArrayList<>();

        for (UUID groupId : groupIds) {
            eventDTOS.addAll(eventStore.findEventDTOsByGroup(Collections.singletonList(groupId.toString())));
        }

        return getEventsFromDTOs(eventDTOS);
    }

    /**
     * Findet alle Events zu Gruppen, welche seit dem neuen Status verändert wurden.
     *
     * @param status Die Id des zuletzt gespeicherten Events
     *
     * @return Liste von neuen und alten Events
     */
    List<UUID> findChangedGroups(long status) {
        List<String> changedGroupIds = eventStore.findGroupIdsWhereEventIdGreaterThanStatus(status);

        log.debug("Seit Event {} haben sich {} Gruppen geändert!", status, changedGroupIds.size());

        return CommonHelper.stringsToUUID(changedGroupIds);
    }

    /**
     * Liefert Gruppen-Ids von existierenden (ungelöschten) Gruppen.
     *
     * @return GruppenIds (UUID) als Liste
     */
    List<UUID> findExistingGroupIds() {
        List<Event> createEvents = findLatestEventsFromGroupsByType(CREATEGROUP,
                                                                    DESTROYGROUP);

        return createEvents.stream()
                           .filter(event -> event instanceof CreateGroupEvent)
                           .map(Event::getGroupid)
                           .collect(Collectors.toList());
    }

    public List<UUID> findPublicGroupIds() {
        List<UUID> groups = findExistingGroupIds();
        List<Event> typeEvents = findLatestEventsFromGroupsByType(SETTYPE);

        typeEvents.removeIf(event -> ((SetTypeEvent) event).getType() == Type.PRIVATE);
        typeEvents.removeIf(event -> !groups.contains(event.getGroupid()));

        return typeEvents.stream()
                         .map(Event::getGroupid)
                         .collect(Collectors.toList());
    }

    public List<UUID> findLectureGroupIds() {
        List<UUID> groups = findExistingGroupIds();
        List<Event> typeEvents = findLatestEventsFromGroupsByType(SETTYPE);

        typeEvents.removeIf(event -> ((SetTypeEvent) event).getType() != Type.LECTURE);
        typeEvents.removeIf(event -> !groups.contains(event.getGroupid()));

        return typeEvents.stream()
                         .map(Event::getGroupid)
                         .collect(Collectors.toList());
    }

    /**
     * Liefert Gruppen-Ids von existierenden (ungelöschten) Gruppen, in welchen der User teilnimmt.
     *
     * <p>
     * Vorgang:
     * Finde für jede Gruppe das letzte Add- oder Kick-Event, welches den User betrifft
     * Finde für jede Gruppe das letzte Destroy-Event
     * Entferne alle alle Events von Gruppen, welche ein Destroy-Event haben
     * Gebe die Gruppen zurück, auf welche sich die Add-Events beziehen
     *
     * @return GruppenIds (UUID) als Liste
     */
    public List<UUID> findExistingUserGroups(String userid) {
        List<Event> userEvents = findLatestEventsFromGroupsByUser(userid);
        List<UUID> deletedIds = findLatestEventsFromGroupsByType(DESTROYGROUP)
                .stream()
                .map(Event::getGroupid)
                .collect(Collectors.toList());

        userEvents.removeIf(event -> deletedIds.contains(event.getGroupid()));

        return userEvents.stream()
                         .filter(event -> event instanceof AddMemberEvent)
                         .map(Event::getGroupid)
                         .collect(Collectors.toList());
    }

    public UUID findGroupByLink(String link) {
        List<Event> groupEvents = findEventsByType(eventTypesToString(SETLINK));

        if (groupEvents.size() > 1) {
            throw new InvalidInviteException("Es existieren mehrere Gruppen mit demselben Link.");
        }

        if (groupEvents.isEmpty()) {
            throw new InvalidInviteException("Link nicht gefunden.");
        }

        return groupEvents.get(0).getGroupid();
    }


    // #################################### SIMPLE QUERIES #######################################


    /**
     * Ermittelt die Id zuletzt gespeicherten Events.
     *
     * @return Letzte EventId
     */
    public long findMaxEventId() {
        try {
            return eventStore.findMaxEventId();
        } catch (NullPointerException e) {
            log.debug("Keine Events vorhanden!");
            return 1;
        }
    }

    List<Event> findEventsByType(String... types) {
        return getEventsFromDTOs(eventStore.findEventDTOsByType(Arrays.asList(types)));
    }

    List<Event> findEventsByType(String type) {
        return getEventsFromDTOs(eventStore.findEventDTOsByType(Collections.singletonList(type)));
    }

    List<Event> findEventsByGroupAndType(List<UUID> groupIds, String... types) {
        return getEventsFromDTOs(eventStore.findEventDTOsByGroupAndType(uuidsToString(groupIds),
                                                                        Arrays.asList(types)));
    }

    /**
     * Sucht zu jeder Gruppe das letzte Add- oder DeleteUserEvent heraus, welches den übergebenen User betrifft.
     *
     * @param userid User, zu welchem die Events gesucht werden
     *
     * @return Eine Liste von einem Add- oder DeleteUserEvent pro Gruppe
     */
    private List<Event> findLatestEventsFromGroupsByUser(String userid) {
        return getEventsFromDTOs(eventStore.findLatestEventDTOsPartitionedByGroupTarget(userid));
    }


    /**
     * Sucht zu jeder Gruppe das letzte Event des/der übergebenen Typen heraus.
     *
     * @param types Eventtyp, nach welchem gesucht wird
     *
     * @return Eine Liste von einem Event pro Gruppe
     */
    private List<Event> findLatestEventsFromGroupsByType(EventType... types) {
        return getEventsFromDTOs(eventStore.findLatestEventDTOsPartitionedByGroupByType(Arrays.asList(eventTypesToString(types))));
    }
}
