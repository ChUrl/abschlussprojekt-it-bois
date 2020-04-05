package mops.gruppen2.controller;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import mops.gruppen2.domain.Group;
import mops.gruppen2.domain.api.GroupRequestWrapper;
import mops.gruppen2.domain.event.Event;
import mops.gruppen2.domain.exception.EventException;
import mops.gruppen2.service.APIService;
import mops.gruppen2.service.EventStoreService;
import mops.gruppen2.service.ProjectionService;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Api zum Datenabgleich mit Gruppenfindung.
 */
//TODO: API-Service?
@RestController
@RequestMapping("/gruppen2/api")
public class APIController {

    private final EventStoreService eventStoreService;
    private final ProjectionService projectionService;

    public APIController(EventStoreService eventStoreService, ProjectionService projectionService) {
        this.eventStoreService = eventStoreService;
        this.projectionService = projectionService;
    }

    @GetMapping("/updateGroups/{lastEventId}")
    @Secured("ROLE_api_user")
    @ApiOperation("Gibt alle Gruppen zurück, in denen sich etwas geändert hat")
    public GroupRequestWrapper updateGroups(@ApiParam("Letzter Status des Anfragestellers") @PathVariable Long lastEventId) throws EventException {
        List<Event> events = eventStoreService.getNewEvents(lastEventId);

        return APIService.wrap(eventStoreService.getMaxEventId(), projectionService.projectEventList(events));
    }

    @GetMapping("/getGroupIdsOfUser/{userId}")
    @Secured("ROLE_api_user")
    @ApiOperation("Gibt alle Gruppen zurück, in denen sich ein Teilnehmer befindet")
    public List<String> getGroupIdsOfUser(@ApiParam("Teilnehmer dessen groupIds zurückgegeben werden sollen") @PathVariable String userId) {
        return projectionService.getUserGroups(userId).stream()
                                .map(group -> group.getId().toString())
                                .collect(Collectors.toList());
    }

    @GetMapping("/getGroup/{groupId}")
    @Secured("ROLE_api_user")
    @ApiOperation("Gibt die Gruppe mit der als Parameter mitgegebenden groupId zurück")
    public Group getGroupById(@ApiParam("GruppenId der gefordeten Gruppe") @PathVariable String groupId) throws EventException {
        List<Event> eventList = eventStoreService.getEventsOfGroup(UUID.fromString(groupId));
        List<Group> groups = projectionService.projectEventList(eventList);

        if (groups.isEmpty()) {
            return null;
        }

        return groups.get(0);
    }

}
