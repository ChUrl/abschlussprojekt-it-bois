package mops.gruppen2.domain.helper;

import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.Group;
import mops.gruppen2.web.api.GroupRequestWrapper;

import java.util.List;

@Log4j2
public final class APIHelper {

    private APIHelper() {}

    public static GroupRequestWrapper wrap(long status, List<Group> groupList) {
        return new GroupRequestWrapper(status, groupList);
    }
}
