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
public class GroupMeta {

    long version;
    String creator;
    LocalDateTime creationDate;

    public GroupMeta setVersion(long version) throws IdMismatchException {
        if (this.version >= version) {
            throw new IdMismatchException("Die Gruppe ist bereits auf einem neueren Stand.");
        }

        return new GroupMeta(version, creator, creationDate);
    }

    public GroupMeta setCreator(String userid) throws BadArgumentException {
        if (creator != null) {
            throw new BadArgumentException("Gruppe hat schon einen Ersteller.");
        }

        return new GroupMeta(version, userid, creationDate);
    }

    public GroupMeta setCreationDate(LocalDateTime date) throws BadArgumentException {
        if (creationDate != null) {
            throw new BadArgumentException("Gruppe hat schon ein Erstellungsdatum.");
        }

        return new GroupMeta(version, creator, date);
    }

    public static GroupMeta EMPTY() {
        return new GroupMeta(0, null, null);
    }
}
