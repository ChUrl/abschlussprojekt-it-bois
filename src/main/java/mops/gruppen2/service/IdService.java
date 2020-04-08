package mops.gruppen2.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Log4j2
public final class IdService {

    private IdService() {}

    public static List<UUID> stringsToUUID(List<String> groupIds) {
        return groupIds.stream()
                       .map(IdService::stringToUUID)
                       .collect(Collectors.toList());
    }

    /**
     * Wandelt einen String in eine UUID um.
     * Dabei wird eine "leere" UUID generiert, falls der String leer ist.
     *
     * @param groupId Id als String
     *
     * @return Id als UUID
     */
    public static UUID stringToUUID(String groupId) {
        return groupId.isEmpty() ? emptyUUID() : UUID.fromString(groupId);
    }

    public static List<String> uuidsToString(List<UUID> groupIds) {
        return groupIds.stream()
                       .map(UUID::toString)
                       .collect(Collectors.toList());
    }

    public static String uuidToString(UUID groupId) {
        return groupId.toString();
    }

    public static boolean isEmpty(UUID id) {
        return id == null || emptyUUID().equals(id);
    }

    public static UUID emptyUUID() {
        return UUID.fromString("00000000-0000-0000-0000-000000000000");
    }
}
