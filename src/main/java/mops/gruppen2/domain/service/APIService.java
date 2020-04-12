package mops.gruppen2.domain.service;

import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.Group;
import mops.gruppen2.web.api.GroupRequestWrapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class APIService {

    private APIService() {}

    public static GroupRequestWrapper wrap(long status, List<Group> groupList) {
        return new GroupRequestWrapper(status, groupList);
    }
}
