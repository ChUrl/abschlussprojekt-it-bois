package mops.gruppen2.domain.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.event.EventType;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommonHelper {

    public static String[] eventTypesToString(EventType... types) {
        String[] stringtypes = new String[types.length];

        for (int i = 0; i < types.length; i++) {
            stringtypes[i] = types[i].toString();
        }

        return stringtypes;
    }

    public static List<String> uuidsToString(List<UUID> ids) {
        return ids.stream()
                  .map(UUID::toString)
                  .collect(Collectors.toList());
    }

    public static boolean uuidIsEmpty(UUID uuid) {
        return "00000000-0000-0000-0000-000000000000".equals(uuid.toString());
    }
}
