package mops.gruppen2.domain.service.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommonHelper {

    public static boolean uuidIsEmpty(UUID uuid) {
        return "00000000-0000-0000-0000-000000000000".equals(uuid.toString());
    }

    public static List<UUID> stringsToUUID(List<String> changedGroupIds) {
        return changedGroupIds.stream()
                              .map(UUID::fromString)
                              .collect(Collectors.toUnmodifiableList());
    }
}
