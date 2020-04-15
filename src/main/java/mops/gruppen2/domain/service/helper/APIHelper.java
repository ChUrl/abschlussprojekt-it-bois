package mops.gruppen2.domain.service.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.infrastructure.api.GroupRequestWrapper;

import java.util.List;

//TODO: sinnvolles format
@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class APIHelper {

    public static GroupRequestWrapper wrap(long status, List<Group> groupList) {
        return new GroupRequestWrapper(status, groupList);
    }
}