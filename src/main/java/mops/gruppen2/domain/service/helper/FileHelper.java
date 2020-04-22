package mops.gruppen2.domain.service.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.event.Event;
import mops.gruppen2.domain.exception.EventException;
import mops.gruppen2.domain.exception.GroupNotFoundException;
import mops.gruppen2.domain.exception.WrongFileException;
import mops.gruppen2.domain.model.group.User;
import mops.gruppen2.persistance.dto.EventDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileHelper {

    // ######################################## CSV #############################################


    public static List<User> readCsvFile(MultipartFile file) throws EventException {
        if (file == null || file.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            List<User> userList = readCsv(file.getInputStream());
            return userList.stream()
                           .distinct()
                           .collect(Collectors.toList()); //filter duplicates from list
        } catch (IOException e) {
            log.error("File konnte nicht gelesen werden!", e);
            throw new WrongFileException(file.getOriginalFilename());
        }
    }

    private static List<User> readCsv(InputStream stream) throws IOException {
        CsvMapper mapper = new CsvMapper();

        CsvSchema schema = mapper.schemaFor(User.class).withHeader().withColumnReordering(true);
        ObjectReader reader = mapper.readerFor(User.class).with(schema);

        return reader.<User>readValues(stream).readAll();
    }

    public static String writeCsvUserList(List<User> members) {
        StringBuilder builder = new StringBuilder();
        builder.append("id,givenname,familyname,email\n");

        members.forEach(user -> builder.append(user.getId())
                                       .append(",")
                                       .append(user.getGivenname())
                                       .append(",")
                                       .append(user.getFamilyname())
                                       .append(",")
                                       .append(user.getEmail())
                                       .append("\n"));

        return builder.toString();
    }


    // ########################################## JSON ###########################################


    /**
     * Übersetzt eine Java-Event-Repräsentation zu einem JSON-Event-Payload.
     *
     * @param event Java-Event-Repräsentation
     *
     * @return JSON-Event-Payload als String
     *
     * @throws JsonProcessingException Bei JSON Fehler
     */

    public static String serializeEventJson(Event event) throws JsonProcessingException {
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
    public static Event deserializeEventJson(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        Event event = mapper.readValue(json, Event.class);
        log.trace(event);
        return event;
    }


    // ############################################### TXT #######################################


    public static String payloadsToPlain(List<String> payloads) {
        return payloads.stream()
                       .map(payload -> payload + "\n")
                       .reduce((String payloadA, String payloadB) -> payloadA + payloadB)
                       .orElseThrow(() -> new GroupNotFoundException("Keine Payloads gefunden."));
    }

    public static String eventDTOsToSql(List<EventDTO> dtos) {
        StringBuilder builder = new StringBuilder();

        builder.append("INSERT INTO event(group_id, group_version, exec_id, target_id, event_date, event_payload)\nVALUES\n");

        dtos.forEach(dto -> builder.append("('")
                                   .append(dto.getGroup_id())
                                   .append("','")
                                   .append(dto.getGroup_version())
                                   .append("','")
                                   .append(dto.getExec_id())
                                   .append("','")
                                   .append(dto.getTarget_id())
                                   .append("','")
                                   .append(dto.getEvent_date())
                                   .append("','")
                                   .append(dto.getEvent_payload())
                                   .append("'),\n"));

        builder.replace(builder.length() - 2, builder.length(), ";");

        return builder.toString();
    }


    // ############################################### SQL #######################################
}
