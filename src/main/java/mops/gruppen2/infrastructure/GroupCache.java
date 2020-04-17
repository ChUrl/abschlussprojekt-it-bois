package mops.gruppen2.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.exception.GroupNotFoundException;
import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.domain.model.group.Type;
import mops.gruppen2.domain.service.EventStoreService;
import mops.gruppen2.domain.service.helper.ProjectionHelper;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
@Component
@Scope("singleton")
public class GroupCache {

    private final EventStoreService eventStoreService;

    private final Map<UUID, Group> groups = new HashMap<>();
    private final Map<String, Group> links = new HashMap<>();
    private final Map<String, List<Group>> users = new HashMap<>();
    private final Map<Type, List<Group>> types = new EnumMap<>(Type.class);


    // ######################################## CACHE ###########################################


    void init() {
        ProjectionHelper.project(groups, eventStoreService.findAllEvents(), this);
    }


    // ########################################### GETTERS #######################################


    public Group group(UUID groupid) {
        if (!groups.containsKey(groupid)) {
            throw new GroupNotFoundException("Gruppe ist nicht im Cache.");
        }

        return groups.get(groupid);
    }

    public Group group(String link) {
        if (!links.containsKey(link)) {
            throw new GroupNotFoundException("Link ist nicht im Cache.");
        }

        return links.get(link);
    }

    public List<Group> userGroups(String userid) {
        if (!users.containsKey(userid)) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(users.get(userid));
    }

    public List<Group> userLectures(String userid) {
        return userGroups(userid).stream()
                                 .filter(Group::isLecture)
                                 .collect(Collectors.toUnmodifiableList());
    }

    public List<Group> userPublics(String userid) {
        return userGroups(userid).stream()
                                 .filter(Group::isPublic)
                                 .collect(Collectors.toUnmodifiableList());
    }

    public List<Group> userPrivates(String userid) {
        return userGroups(userid).stream()
                                 .filter(Group::isPrivate)
                                 .collect(Collectors.toUnmodifiableList());
    }

    public List<Group> publics() {
        if (!types.containsKey(Type.PUBLIC)) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(types.get(Type.PUBLIC));
    }

    public List<Group> privates() {
        if (!types.containsKey(Type.PRIVATE)) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(types.get(Type.PRIVATE));
    }

    public List<Group> lectures() {
        if (!types.containsKey(Type.LECTURE)) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(types.get(Type.LECTURE));
    }


    // ######################################## SETTERS ##########################################


    public void usersPut(String userid, Group group) {
        if (!users.containsKey(userid)) {
            users.put(userid, new ArrayList<>());
            log.debug("Ein User wurde dem Cache hinzugefügt.");
        }

        users.get(userid).add(group);
    }

    public void usersRemove(String target, Group group) {
        if (!users.containsKey(target)) {
            return;
        }

        users.get(target).remove(group);
    }

    public void groupsPut(UUID groupid, Group group) {
        groups.put(groupid, group);
    }

    public void groupsRemove(Group group) {
        groups.remove(group.getId());
    }

    public void linksPut(String link, Group group) {
        links.put(link, group);
    }

    public void linksRemove(String link) {
        links.remove(link);
    }

    public void typesPut(Type type, Group group) {
        if (!types.containsKey(type)) {
            types.put(type, new ArrayList<>());
            log.debug("Ein Typ wurde dem Cache hinzugefügt.");
        }

        types.get(type).add(group);
    }

    public void typesRemove(Group group) {
        if (!types.containsKey(group.getType())) {
            return;
        }

        types.get(group.getType()).remove(group);
    }
}
