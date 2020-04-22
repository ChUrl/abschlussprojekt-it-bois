package mops.gruppen2.domain.model.group;

import lombok.ToString;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.exception.BadArgumentException;
import mops.gruppen2.domain.exception.IdMismatchException;

import java.time.LocalDateTime;

@Log4j2
@Value
@ToString
class GroupMeta {

    long version;
    String creator;
    LocalDateTime creationDate;

    GroupMeta setVersion(long version) throws IdMismatchException {
        if (this.version >= version) {
            throw new IdMismatchException("Die Gruppe ist bereits auf einem neueren Stand.");
        }
        if (this.version + 1 != version) {
            throw new IdMismatchException("Es fehlen vorherige Events.");
        }

        return new GroupMeta(version, creator, creationDate);
    }

    GroupMeta setCreator(String userid) throws BadArgumentException {
        if (creator != null) {
            throw new BadArgumentException("Gruppe hat schon einen Ersteller.");
        }

        return new GroupMeta(version, userid, creationDate);
    }

    GroupMeta setCreationDate(LocalDateTime date) throws BadArgumentException {
        if (creationDate != null) {
            throw new BadArgumentException("Gruppe hat schon ein Erstellungsdatum.");
        }

        return new GroupMeta(version, creator, date);
    }

    static GroupMeta EMPTY() {
        return new GroupMeta(0, null, null);
    }
}
