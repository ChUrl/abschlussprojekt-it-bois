package mops.gruppen2.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import mops.gruppen2.domain.dto.EventDTO;
import mops.gruppen2.domain.event.Event;
import mops.gruppen2.domain.exception.BadPayloadException;
import mops.gruppen2.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
//TODO: Evtl aufsplitten in EventRepoService und EventService?
public class EventStoreService {

    private static final Logger LOG = LoggerFactory.getLogger(EventStoreService.class);
    private final EventRepository eventStore;

    public EventStoreService(EventRepository eventStore) {
        this.eventStore = eventStore;
    }

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

    /**
     * Erzeugt aus einem Event Objekt ein EventDTO Objekt.
     *
     * @param event Event, welches in DTO übersetzt wird
     *
     * @return EventDTO (Neues DTO)
     */
    public static EventDTO getDTOFromEvent(Event event) {
        String payload = "";
        try {
            payload = JsonService.serializeEvent(event);
        } catch (JsonProcessingException e) {
            LOG.error("Event ({}) konnte nicht serialisiert werden!", event.getClass());
        }

        return new EventDTO(null, event.getGroupId().toString(), event.getUserId(), getEventType(event), payload);
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

    static Event getEventFromDTO(EventDTO dto) throws BadPayloadException {
        try {
            return JsonService.deserializeEvent(dto.getEvent_payload());
        } catch (JsonProcessingException e) {
            LOG.error("Payload\n {}\n konnte nicht deserialisiert werden!", dto.getEvent_payload());
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

    /**
     * Sucht in der DB alle Zeilen raus welche eine der Gruppen_ids hat.
     * Wandelt die Zeilen in Events um und gibt davon eine Liste zurück.
     *
     * @param groupIds Liste an IDs
     *
     * @return Liste an Events
     */
    //TODO: EventStoreService
    public List<Event> getGroupEvents(List<UUID> groupIds) {
        List<EventDTO> eventDTOS = new ArrayList<>();
        for (UUID groupId : groupIds) {
            eventDTOS.addAll(eventStore.findEventDTOByGroupId(groupId.toString()));
        }
        return getEventsFromDTOs(eventDTOS);
    }

    /**
     * Findet alle Events welche ab dem neuen Status hinzugekommen sind.
     * Sucht alle Events mit event_id > status
     *
     * @param status Die Id des zuletzt gespeicherten Events
     *
     * @return Liste von neueren Events
     */
    public List<Event> getNewEvents(Long status) {
        List<String> groupIdsThatChanged = eventStore.findNewEventSinceStatus(status);

        List<EventDTO> groupEventDTOS = eventStore.findAllEventsOfGroups(groupIdsThatChanged);
        return getEventsFromDTOs(groupEventDTOS);
    }

    public long getMaxEventId() {
        long highestEvent = 0;

        try {
            highestEvent = eventStore.getHighesEventID();
        } catch (NullPointerException e) {
            LOG.debug("Eine maxId von 0 wurde zurückgegeben, da keine Events vorhanden sind.");
        }

        return highestEvent;
    }

    /**
     * Gibt eine Liste mit allen Events zurück, die zu der Gruppe gehören.
     *
     * @param groupId Gruppe die betrachtet werden soll
     *
     * @return Liste aus Events
     */
    public List<Event> getEventsOfGroup(UUID groupId) {
        List<EventDTO> eventDTOList = eventStore.findEventDTOByGroupId(groupId.toString());
        return getEventsFromDTOs(eventDTOList);
    }

    /**
     * Gibt eine Liste aus GruppenIds zurück, in denen sich der User befindet.
     *
     * @param userId Die Id des Users
     *
     * @return Liste aus GruppenIds
     */
    public List<UUID> findGroupIdsByUser(String userId) {
        return eventStore.findGroupIdsWhereUserId(userId, "AddUserEvent").stream().map(UUID::fromString).collect(Collectors.toList());
    }

    /**
     * Gibt true zurück, falls der User aktuell in der Gruppe ist, sonst false.
     *
     * @param groupId Id der Gruppe
     * @param userId  Id des zu überprüfenden Users
     *
     * @return true or false
     */
    boolean userInGroup(UUID groupId, String userId) {
        return eventStore.countEventsByGroupIdAndUserIdAndEventType(groupId.toString(), userId, "AddUserEvent") > eventStore.countEventsByGroupIdAndUserIdAndEventType(groupId.toString(), userId, "DeleteUserEvent");
    }
}
