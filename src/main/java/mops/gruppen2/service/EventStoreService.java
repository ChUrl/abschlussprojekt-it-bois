package mops.gruppen2.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.dto.EventDTO;
import mops.gruppen2.domain.event.AddUserEvent;
import mops.gruppen2.domain.event.CreateGroupEvent;
import mops.gruppen2.domain.event.Event;
import mops.gruppen2.domain.exception.BadPayloadException;
import mops.gruppen2.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Log4j2
public class EventStoreService {

    private final EventRepository eventStore;

    public EventStoreService(EventRepository eventStore) {
        this.eventStore = eventStore;
    }


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


    static List<EventDTO> getDTOsFromEvents(List<Event> events) {
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
    static EventDTO getDTOFromEvent(Event event) {
        try {
            String payload = JsonService.serializeEvent(event);
            return new EventDTO(null,
                                event.getGroupId().toString(),
                                event.getUserId(),
                                getEventType(event),
                                payload);
        } catch (JsonProcessingException e) {
            log.error("Event ({}) konnte nicht serialisiert werden!", e.getMessage());
            e.printStackTrace();
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
    static List<Event> getEventsFromDTOs(List<EventDTO> eventDTOS) {
        return eventDTOS.stream()
                        .map(EventStoreService::getEventFromDTO)
                        .collect(Collectors.toList());
    }

    static Event getEventFromDTO(EventDTO dto) {
        try {
            return JsonService.deserializeEvent(dto.getEvent_payload());
        } catch (JsonProcessingException e) {
            log.error("Payload\n {}\n konnte nicht deserialisiert werden!", e.getMessage());
            e.printStackTrace();
            throw new BadPayloadException(EventStoreService.class.toString());
        }
    }

    /**
     * Gibt den Eventtyp als String wieder.
     *
     * @param event Event dessen Typ abgefragt werden soll
     *
     * @return Der Name des Typs des Events
     */
    private static String getEventType(Event event) {
        int lastDot = event.getClass().getName().lastIndexOf('.');

        return event.getClass().getName().substring(lastDot + 1);
    }


    // ######################################## QUERIES ##########################################


    List<Event> findGroupEvents(UUID groupId) {
        return getEventsFromDTOs(eventStore.findEventDTOsByGroup(Collections.singletonList(groupId.toString())));
    }

    /**
     * Sucht alle Events, welche zu einer der übergebenen Gruppen gehören
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
    List<Event> findChangedGroupEvents(long status) {
        List<String> changedGroupIds = eventStore.findGroupIdsWhereEventIdGreaterThanStatus(status);
        List<EventDTO> groupEventDTOS = eventStore.findEventDTOsByGroup(changedGroupIds);

        log.trace("Seit Event {} haben sich {} Gruppen geändert!", status, changedGroupIds.size());

        return getEventsFromDTOs(groupEventDTOS);
    }

    /**
     * Liefert Gruppen-Ids von existierenden (ungelöschten) Gruppen.
     *
     * @return GruppenIds (UUID) als Liste
     */
    List<UUID> findExistingGroupIds() {
        List<Event> createEvents = findLatestEventsFromGroupsByType("CreateGroupEvent",
                                                                    "DeleteGroupEvent");

        return createEvents.stream()
                           .filter(event -> event instanceof CreateGroupEvent)
                           .map(Event::getGroupId)
                           .collect(Collectors.toList());
    }

    /**
     * Liefert Gruppen-Ids von existierenden (ungelöschten) Gruppen, in welchen der User teilnimmt.
     *
     * @return GruppenIds (UUID) als Liste
     */
    public List<UUID> findExistingUserGroups(String userId) {
        List<Event> userEvents = findLatestEventsFromGroupsByUser(userId);

        return userEvents.stream()
                         .filter(event -> event instanceof AddUserEvent)
                         .map(Event::getGroupId)
                         .collect(Collectors.toList());
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
            log.trace("Eine maxId von 0 wurde zurückgegeben, da keine Events vorhanden sind.");
            e.printStackTrace();
            return 0;
        }
    }

    List<Event> findEventsByType(String... types) {
        return getEventsFromDTOs(eventStore.findEventDTOsByType(Arrays.asList(types)));
    }

    List<Event> findEventsByType(String type) {
        return getEventsFromDTOs(eventStore.findEventDTOsByType(Collections.singletonList(type)));
    }

    List<Event> findEventsByGroupAndType(List<UUID> groupIds, String... types) {
        return getEventsFromDTOs(eventStore.findEventDTOsByGroupAndType(Arrays.asList(types),
                                                                        IdService.uuidToString(groupIds)));
    }

    /**
     * Sucht zu jeder Gruppe das letzte Add- oder DeleteUserEvent heraus, welches den übergebenen User betrifft.
     *
     * @param userId User, zu welchem die Events gesucht werden
     *
     * @return Eine Liste von einem Add- oder DeleteUserEvent pro Gruppe
     */
    List<Event> findLatestEventsFromGroupsByUser(String userId) {
        return getEventsFromDTOs(eventStore.findLatestEventDTOsPartitionedByGroupByUser(userId));
    }


    /**
     * Sucht zu jeder Gruppe das letzte Event des/der übergebenen Typen heraus.
     *
     * @param types Eventtyp, nach welchem gesucht wird
     *
     * @return Eine Liste von einem Event pro Gruppe
     */
    List<Event> findLatestEventsFromGroupsByType(String... types) {
        return getEventsFromDTOs(eventStore.findLatestEventDTOsPartitionedByGroupByType(Arrays.asList(types)));
    }
}
