package mops.gruppen2.domain.service.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.UUID;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommonHelper {

    public static boolean uuidIsEmpty(UUID uuid) {
        return "00000000-0000-0000-0000-000000000000".equals(uuid.toString());
    }
}
