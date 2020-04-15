package mops.gruppen2.domain.service.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.event.Event;

/**
 * Übersetzt JSON-Event-Payloads zu Java-Event-Repräsentationen und zurück.
 */
@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonHelper {

    /**
     * Übersetzt eine Java-Event-Repräsentation zu einem JSON-Event-Payload.
     *
     * @param event Java-Event-Repräsentation
     *
     * @return JSON-Event-Payload als String
     *
     * @throws JsonProcessingException Bei JSON Fehler
     */

    public static String serializeEvent(Event event) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        String payload = mapper.writeValueAsString(event);
        log.trace(payload);
        return payload;
    }

    /**
     * Übersetzt eine JSON-Event-Payload zu einer Java-Event-Repräsentation.
     *
     * @param json JSON-Event-Payload als String
     *
     * @return Java-Event-Repräsentation
     *
     * @throws JsonProcessingException Bei JSON Fehler
     */
    public static Event deserializeEvent(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        Event event = mapper.readValue(json, Event.class);
        log.trace(event);
        return event;
    }
}
