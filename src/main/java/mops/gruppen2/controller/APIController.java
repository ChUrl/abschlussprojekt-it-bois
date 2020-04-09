package mops.gruppen2.controller;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.aspect.annotation.TraceMethodCalls;
import mops.gruppen2.domain.Group;
import mops.gruppen2.domain.User;
import mops.gruppen2.domain.api.GroupRequestWrapper;
import mops.gruppen2.service.APIService;
import mops.gruppen2.service.EventStoreService;
import mops.gruppen2.service.IdService;
import mops.gruppen2.service.ProjectionService;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Api zum Datenabgleich.
 */
@Log4j2
@TraceMethodCalls
@RestController
@RequestMapping("/gruppen2/api")
public class APIController {

    private final EventStoreService eventStoreService;
    private final ProjectionService projectionService;

    public APIController(EventStoreService eventStoreService, ProjectionService projectionService) {
        this.eventStoreService = eventStoreService;
        this.projectionService = projectionService;
    }

    /**
     * Erzeugt eine Liste aus Gruppen, welche sich seit einer übergebenen Event-Id geändert haben.
     * Die Gruppen werden vollständig projiziert, enthalten also alle Informationen zum entsprechenden Zeitpunkt.
     *
     * @param eventId Die Event-ID, welche der Anfragesteller beim letzten Aufruf erhalten hat
     */
    @GetMapping("/update/{id}")
    @Secured("ROLE_api_user")
    @ApiOperation("Gibt veränderte Gruppen zurück")
    public GroupRequestWrapper update(@ApiParam("Letzte gespeicherte EventId des Anfragestellers")
                                      @PathVariable("id") long eventId) {

        return APIService.wrap(eventStoreService.findMaxEventId(),
                               projectionService.projectNewGroups(eventId));
    }

    /**
     * Gibt die Gruppen-IDs von Gruppen, in welchen der übergebene Nutzer teilnimmt, zurück.
     */
    @GetMapping("/usergroups/{id}")
    @Secured("ROLE_api_user")
    @ApiOperation("Gibt Gruppen zurück, in welchen ein Nutzer teilnimmt")
    public List<String> usergroups(@ApiParam("Nutzer-Id")
                                   @PathVariable("id") String userId) {

        return IdService.uuidsToString(eventStoreService.findExistingUserGroups(new User(userId)));
    }

    /**
     * Konstruiert eine einzelne, vollständige Gruppe.
     */
    @GetMapping("/group/{id}")
    @Secured("ROLE_api_user")
    @ApiOperation("Gibt die Gruppe mit der als Parameter mitgegebenden groupId zurück")
    public Group getGroupById(@ApiParam("Gruppen-Id der gefordeten Gruppe")
                              @PathVariable("id") String groupId) {

        return projectionService.projectSingleGroup(UUID.fromString(groupId));
    }

}
