package mops.gruppen2.service;

import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.GroupType;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@Log4j2
public final class ControllerService {

    private ControllerService() {}

    public static GroupType getGroupType(String type) {
        return GroupType.valueOf(type);
    }

    /**
     * Ermittelt die UUID des Parents, falls vorhanden.
     */
    public static UUID getParent(String parent, String type) {
        return GroupType.valueOf(type) == GroupType.LECTURE ? IdService.emptyUUID() : IdService.stringToUUID(parent);
    }
}
