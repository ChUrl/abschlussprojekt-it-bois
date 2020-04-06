package mops.gruppen2.controller;

import mops.gruppen2.domain.Account;
import mops.gruppen2.domain.Group;
import mops.gruppen2.domain.User;
import mops.gruppen2.domain.Visibility;
import mops.gruppen2.service.GroupService;
import mops.gruppen2.service.InviteService;
import mops.gruppen2.service.ProjectionService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@SessionScope
@RequestMapping("/gruppen2")
public class SearchAndInviteController {

    private final ValidationService validationService;
    private final InviteService inviteService;
    private final GroupService groupService;
    private final ProjectionService projectionService;

    public SearchAndInviteController(ValidationService validationService, InviteService inviteService, GroupService groupService, ProjectionService projectionService) {
        this.validationService = validationService;
        this.inviteService = inviteService;
        this.groupService = groupService;
        this.projectionService = projectionService;
    }

    @RolesAllowed({"ROLE_orga", "ROLE_studentin", "ROLE_actuator"})
    @GetMapping("/findGroup")
    public String findGroup(KeycloakAuthenticationToken token,
                            Model model,
                            @RequestParam(value = "suchbegriff", required = false) String search) {

        Account account = new Account(token);
        List<Group> groups = new ArrayList<>();
        groups = validationService.checkSearch(search, groups, account);

        model.addAttribute("account", account);
        model.addAttribute("gruppen", groups);
        model.addAttribute("inviteService", inviteService);

        return "search";
    }

    @RolesAllowed({"ROLE_orga", "ROLE_studentin", "ROLE_actuator"})
    @GetMapping("/detailsSearch")
    public String showGroupDetailsNoMember(KeycloakAuthenticationToken token,
                                           Model model,
                                           @RequestParam("id") String groupId) {

        Account account = new Account(token);
        Group group = projectionService.projectSingleGroup(UUID.fromString(groupId));
        UUID parentId = group.getParent();
        Group parent = groupService.getParent(parentId);
        User user = new User(account);

        model.addAttribute("account", account);
        if (validationService.checkIfUserInGroup(group, user)) {
            return "redirect:/gruppen2/details/" + groupId;
        }

        model.addAttribute("group", group);
        model.addAttribute("parentId", parentId);
        model.addAttribute("parent", parent);

        return "detailsNoMember";
    }

    @RolesAllowed({"ROLE_orga", "ROLE_studentin", "ROLE_actuator"})
    @GetMapping("/acceptinvite/{link}")
    public String acceptInvite(KeycloakAuthenticationToken token,
                               Model model,
                               @PathVariable("link") String link) {

        Group group = projectionService.projectSingleGroup(inviteService.getGroupIdFromLink(link));

        validationService.throwIfGroupNotExisting(group.getTitle());

        model.addAttribute("account", new Account(token));
        model.addAttribute("group", group);

        if (group.getVisibility() == Visibility.PUBLIC) {
            return "redirect:/gruppen2/details/" + group.getId();
        }

        return "joinprivate";
    }

    @RolesAllowed({"ROLE_orga", "ROLE_studentin", "ROLE_actuator"})
    @PostMapping("/acceptinvite")
    @CacheEvict(value = "groups", allEntries = true)
    public String postAcceptInvite(KeycloakAuthenticationToken token,
                                   @RequestParam("id") String groupId) {

        Account account = new Account(token);
        User user = new User(account);
        Group group = projectionService.projectSingleGroup(UUID.fromString(groupId));

        validationService.throwIfUserAlreadyInGroup(group, user);
        validationService.throwIfGroupFull(group);

        groupService.addUser(account, UUID.fromString(groupId));

        return "redirect:/gruppen2/details/" + groupId;
    }
}
