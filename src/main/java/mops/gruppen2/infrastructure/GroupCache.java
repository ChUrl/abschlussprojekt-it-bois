package mops.gruppen2.infrastructure;

import lombok.RequiredArgsConstructor;
import mops.gruppen2.domain.exception.GroupNotFoundException;
import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.domain.service.EventStoreService;
import mops.gruppen2.domain.service.helper.ProjectionHelper;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
@Scope("singleton")
public class GroupCache {

    private final EventStoreService eventStoreService;

    private final Map<UUID, Group> groups = new HashMap<>();

    public void init() {
        ProjectionHelper.project(groups, eventStoreService.findAllEvents(), this);
    }

    public void put(Group group) {
        groups.put(group.getId(), group);
    }

    public void remove(Group group) {
        groups.remove(group.getId());
    }

    // Getters

    public Group group(UUID groupid) {
        if (!groups.containsKey(groupid)) {
            throw new GroupNotFoundException("Gruppe ist nicht im Cache.");
        }

        return groups.get(groupid);
    }

    public Group group(String link) {
        return groups.values().stream()
                     .filter(group -> group.getLink().equals(link))
                     .findFirst()
                     .orElseThrow(() -> new GroupNotFoundException("Link nicht im Cache."));
    }

    public List<Group> userGroups(String userid) {
        return groups.values().stream()
                     .filter(group -> group.isMember(userid))
                     .collect(Collectors.toUnmodifiableList());
    }

    public List<Group> publics() {
        return groups.values().stream()
                     .filter(Group::isPublic)
                     .collect(Collectors.toUnmodifiableList());
    }

    public List<Group> privates() {
        return groups.values().stream()
                     .filter(Group::isPrivate)
                     .collect(Collectors.toUnmodifiableList());
    }

    public List<Group> lectures() {
        return groups.values().stream()
                     .filter(Group::isLecture)
                     .collect(Collectors.toUnmodifiableList());
    }
}
