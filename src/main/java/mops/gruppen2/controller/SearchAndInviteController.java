package mops.gruppen2.controller;

import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.Account;
import mops.gruppen2.domain.Group;
import mops.gruppen2.domain.GroupType;
import mops.gruppen2.domain.User;
import mops.gruppen2.domain.Visibility;
import mops.gruppen2.service.GroupService;
import mops.gruppen2.service.InviteService;
import mops.gruppen2.service.ProjectionService;
import mops.gruppen2.service.SearchService;
import mops.gruppen2.service.ValidationService;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.annotation.SessionScope;

import javax.annotation.security.RolesAllowed;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("SameReturnValue")
@Controller
@SessionScope
@RequestMapping("/gruppen2")
@Log4j2
public class SearchAndInviteController {

    private final InviteService inviteService;
    private final GroupService groupService;
    private final ProjectionService projectionService;
    private final SearchService searchService;

    public SearchAndInviteController(InviteService inviteService, GroupService groupService, ProjectionService projectionService, SearchService searchService) {
        this.inviteService = inviteService;
        this.groupService = groupService;
        this.projectionService = projectionService;
        this.searchService = searchService;
    }

    @RolesAllowed({"ROLE_orga", "ROLE_studentin", "ROLE_actuator"})
    @GetMapping("/searchPage")
    public String findGroup(KeycloakAuthenticationToken token,
                            Model model) {

        log.info("GET to /searchPage\n");

        Account account = new Account(token);

        model.addAttribute("account", account);
        model.addAttribute("gruppen", Collections.emptyList()); // TODO: verschönern
        model.addAttribute("inviteService", inviteService); //TODO: don't inject service

        return "search";
    }

    @RolesAllowed({"ROLE_orga", "ROLE_studentin", "ROLE_actuator"})
    @GetMapping("/search")
    public String search(KeycloakAuthenticationToken token,
                         Model model,
                         @RequestParam("suchbegriff") String search) {

        log.info("GET to /search\n");

        Account account = new Account(token);
        User user = new User(account);

        List<Group> groups = searchService.searchPublicGroups(search, user);

        model.addAttribute("account", account);
        model.addAttribute("gruppen", groups);
        model.addAttribute("inviteService", inviteService); //TODO: don't inject service

        return "search";
    }

    @RolesAllowed({"ROLE_orga", "ROLE_studentin", "ROLE_actuator"})
    @GetMapping("/detailsSearch")
    public String showGroupDetailsNoMember(KeycloakAuthenticationToken token,
                                           Model model,
                                           @RequestParam("id") String groupId) {

        log.info("GET to /detailsSearch\n");

        Account account = new Account(token);
        User user = new User(account);

        Group group = projectionService.projectSingleGroup(UUID.fromString(groupId));

        // Parent Badge
        UUID parentId = group.getParent();
        Group parent = projectionService.projectParent(parentId);

        model.addAttribute("account", account);
        if (ValidationService.checkIfMember(group, user)) {
            return "redirect:/gruppen2/details/" + groupId;
        }

        model.addAttribute("group", group);
        model.addAttribute("parentId", parentId);
        model.addAttribute("parent", parent);
        model.addAttribute("lecture", GroupType.LECTURE);

        return "detailsNoMember";
    }

    @RolesAllowed({"ROLE_orga", "ROLE_studentin", "ROLE_actuator"})
    @GetMapping("/acceptinvite/{link}")
    public String acceptInvite(KeycloakAuthenticationToken token,
                               Model model,
                               @PathVariable("link") String link) {

        log.info("GET to /acceptInvite\n");

        Account account = new Account(token);
        User user = new User(account);

        Group group = projectionService.projectSingleGroup(inviteService.getGroupIdFromLink(link));

        model.addAttribute("account", account);
        model.addAttribute("group", group);

        // Gruppe öffentlich
        if (group.getVisibility() == Visibility.PUBLIC) {
            return "redirect:/gruppen2/details/" + group.getId();
        }

        // Bereits Mitglied
        if (ValidationService.checkIfMember(group, user)) {
            return "redirect:/gruppen2/details/" + group.getId();
        }

        return "joinprivate";
    }

    @RolesAllowed({"ROLE_orga", "ROLE_studentin", "ROLE_actuator"})
    @PostMapping("/acceptinvite")
    @CacheEvict(value = "groups", allEntries = true)
    public String postAcceptInvite(KeycloakAuthenticationToken token,
                                   @RequestParam("id") String groupId) {

        log.info("POST to /acceptInvite\n");

        Account account = new Account(token);
        User user = new User(account);
        Group group = projectionService.projectSingleGroup(UUID.fromString(groupId));

        ValidationService.throwIfMember(group, user);
        ValidationService.throwIfGroupFull(group);

        groupService.addUser(user, group);

        return "redirect:/gruppen2/details/" + groupId;
    }
}
