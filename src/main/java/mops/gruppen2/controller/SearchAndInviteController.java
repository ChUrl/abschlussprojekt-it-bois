package mops.gruppen2.controller;

import lombok.extern.log4j.Log4j2;
import mops.gruppen2.aspect.annotation.TraceMethodCalls;
import mops.gruppen2.domain.Group;
import mops.gruppen2.domain.GroupType;
import mops.gruppen2.domain.User;
import mops.gruppen2.service.InviteService;
import mops.gruppen2.service.ProjectionService;
import mops.gruppen2.service.SearchService;
import mops.gruppen2.service.ValidationService;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.security.RolesAllowed;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("SameReturnValue")
@Log4j2
@TraceMethodCalls
@Controller
@RequestMapping("/gruppen2")
public class SearchAndInviteController {

    private final InviteService inviteService;
    private final ProjectionService projectionService;
    private final SearchService searchService;

    public SearchAndInviteController(InviteService inviteService, ProjectionService projectionService, SearchService searchService) {
        this.inviteService = inviteService;
        this.projectionService = projectionService;
        this.searchService = searchService;
    }

    @RolesAllowed({"ROLE_orga", "ROLE_studentin"})
    @GetMapping("/search")
    public String getSearch(Model model) {
        // Noch keine Suche gestartet: leeres Suchergebnis
        model.addAttribute("gruppen", Collections.emptyList());

        return "search";
    }

    @RolesAllowed({"ROLE_orga", "ROLE_studentin"})
    @PostMapping("/search")
    public String postSearch(KeycloakAuthenticationToken token,
                             Model model,
                             @RequestParam("string") String search) {

        User user = new User(token);
        List<Group> groups = searchService.searchPublicGroups(search, user);

        model.addAttribute("groups", groups);

        return "search";
    }

    @RolesAllowed({"ROLE_orga", "ROLE_studentin"})
    @GetMapping("/join/{link}")
    public String getJoin(KeycloakAuthenticationToken token,
                          Model model,
                          @PathVariable("link") String link) {

        User user = new User(token);
        Group group = projectionService.projectSingleGroup(inviteService.getGroupIdFromLink(link));

        model.addAttribute("group", group);

        // Gruppe öffentlich
        if (group.getType() == GroupType.PUBLIC) {
            return "redirect:/gruppen2/details/" + group.getId();
        }

        // Bereits Mitglied
        if (ValidationService.checkIfMember(group, user)) {
            return "redirect:/gruppen2/details/" + group.getId();
        }

        return "join";
    }
}
