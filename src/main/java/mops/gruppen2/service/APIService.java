package mops.gruppen2.service;

import mops.gruppen2.domain.Group;
import mops.gruppen2.domain.api.GroupRequestWrapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class APIService {

    // private APIService() {}

    public static GroupRequestWrapper wrap(long status, List<Group> groupList) {
        return new GroupRequestWrapper(status, groupList);
    }

    // public static void updateGroups()

    // public static void getGroupIdsOfUser()

    // public static void getGroupById()

    // public static void updateNecessary()
}
