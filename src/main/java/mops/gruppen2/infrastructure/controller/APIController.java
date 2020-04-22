package mops.gruppen2.infrastructure.controller;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.aspect.annotation.TraceMethodCalls;
import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.domain.service.EventStoreService;
import mops.gruppen2.domain.service.helper.APIHelper;
import mops.gruppen2.domain.service.helper.ProjectionHelper;
import mops.gruppen2.infrastructure.GroupCache;
import mops.gruppen2.infrastructure.api.GroupRequestWrapper;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Api zum Datenabgleich.
 */
@Log4j2
@TraceMethodCalls
@RequiredArgsConstructor
@RestController
@RequestMapping("/gruppen2/api")
public class APIController {

    private final GroupCache cache;
    private final EventStoreService eventStoreService;

    /**
     * Erzeugt eine Liste aus Gruppen, welche sich seit einer übergebenen Event-Id geändert haben.
     * Die Gruppen werden vollständig projiziert, enthalten also alle Informationen zum entsprechenden Zeitpunkt.
     *
     * @param eventId Die Event-ID, welche der Anfragesteller beim letzten Aufruf erhalten hat
     */
    //TODO: sollte den cache benutzen, am besten wäre eine groupversion, welche der eventid
    //TODO: entspricht, dann kann man leicht alle geänderten gruppen finden
    @GetMapping("/update/{id}")
    @Secured("ROLE_api_user")
    @ApiOperation("Gibt veränderte Gruppen zurück")
    public GroupRequestWrapper getApiUpdate(@ApiParam("Letzte gespeicherte EventId des Anfragestellers")
                                            @PathVariable("id") long eventId) {

        return APIHelper.wrap(eventStoreService.findMaxEventId(),
                              ProjectionHelper.project(eventStoreService.findGroupEvents(eventStoreService.findChangedGroups(eventId))));
    }

    /**
     * Gibt die Gruppen-IDs von Gruppen, in welchen der übergebene Nutzer teilnimmt, zurück.
     */
    @GetMapping("/usergroups/{id}")
    @Secured("ROLE_api_user")
    @ApiOperation("Gibt Gruppen zurück, in welchen ein Nutzer teilnimmt")
    public List<String> getApiUserGroups(@ApiParam("Nutzer-Id")
                                         @PathVariable("id") String userId) {

        return cache.userGroups(userId).stream()
                    .map(Group::getId)
                    .map(UUID::toString)
                    .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Konstruiert eine einzelne, vollständige Gruppe.
     */
    @GetMapping("/group/{id}")
    @Secured("ROLE_api_user")
    @ApiOperation("Gibt die Gruppe mit der als Parameter mitgegebenden groupId zurück")
    public Group getApiGroup(@ApiParam("Gruppen-Id der gefordeten Gruppe")
                             @PathVariable("id") String groupId) {

        return cache.group(UUID.fromString(groupId));
    }

}
