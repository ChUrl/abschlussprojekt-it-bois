package mops.gruppen2.service;

import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.GroupType;
import mops.gruppen2.domain.Visibility;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@Log4j2
public final class ControllerService {

    private ControllerService() {}

    public static Visibility getVisibility(boolean isPrivate) {
        return isPrivate ? Visibility.PRIVATE : Visibility.PUBLIC;
    }

    public static GroupType getGroupType(boolean isLecture) {
        return isLecture ? GroupType.LECTURE : GroupType.SIMPLE;
    }

    /**
     * Wenn die maximale Useranzahl unendlich ist, wird das Maximum auf 100000 gesetzt.
     * Praktisch gibt es also maximal 100000 Nutzer pro Gruppe.
     *
     * @param isInfinite Gibt an, ob es unendlich viele User geben soll
     * @param userLimit  Das Maximum an Usern, falls es eins gibt
     *
     * @return Maximum an Usern
     */
    public static long getUserLimit(boolean isInfinite, long userLimit) {
        return isInfinite ? Long.MAX_VALUE : userLimit;
    }

    /**
     * Ermittelt die UUID des Parents, falls vorhanden.
     */
    public static UUID getParent(String parent, boolean isLecture) {
        return isLecture ? IdService.emptyUUID() : IdService.stringToUUID(parent);
    }
}
