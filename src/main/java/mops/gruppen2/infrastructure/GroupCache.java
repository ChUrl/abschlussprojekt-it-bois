package mops.gruppen2.infrastructure;

import lombok.RequiredArgsConstructor;
import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.domain.service.ProjectionService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Component
@Scope("singleton")
public class GroupCache {

    private final ProjectionService projectionService;

    private long version;
    private boolean isValid;
    private String principal;

    private Map<UUID, Group> groups;

    private List<UUID> userGroups;
    private List<UUID> publics;
    private List<UUID> privates;
    private List<UUID> lectures;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        groups = projectionService.projectAllGroups();

        Group current;
        UUID currentId;
        for (Map.Entry<UUID, Group> entry : groups.entrySet()) {
            current = entry.getValue();
            currentId = entry.getKey();

            if (current.isMember(principal)) {
                userGroups.add(currentId);
            }
            if (current.isPublic()) {
                publics.add(currentId);
            }
            if (current.isPrivate()) {
                privates.add(currentId);
            }
            if (current.isLecture()) {
                lectures.add(currentId);
            }
        }
    }
}
