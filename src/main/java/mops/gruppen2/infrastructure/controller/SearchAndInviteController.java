package mops.gruppen2.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.aspect.annotation.TraceMethodCalls;
import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.domain.model.group.Type;
import mops.gruppen2.domain.service.SearchService;
import mops.gruppen2.infrastructure.GroupCache;
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
@RequiredArgsConstructor
@Controller
@RequestMapping("/gruppen2")
public class SearchAndInviteController {

    private final GroupCache groupCache;
    private final SearchService searchService;

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

        String principal = token.getName();
        List<Group> groups = searchService.search(search, principal);

        model.addAttribute("groups", groups);

        return "search";
    }

    @RolesAllowed({"ROLE_orga", "ROLE_studentin"})
    @GetMapping("/join/{link}")
    public String getJoin(KeycloakAuthenticationToken token,
                          Model model,
                          @PathVariable("link") String link) {

        String principal = token.getName();
        Group group = groupCache.group(link);

        model.addAttribute("group", group);

        // Gruppe öffentlich
        if (group.getType() == Type.PUBLIC) {
            return "redirect:/gruppen2/details/" + group.getId();
        }

        // Bereits Mitglied
        if (group.isMember(principal)) {
            return "redirect:/gruppen2/details/" + group.getId();
        }

        return "link";
    }
}
