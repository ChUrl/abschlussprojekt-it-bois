package mops.gruppen2.controller;

import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.Account;
import mops.gruppen2.domain.Group;
import mops.gruppen2.domain.Role;
import mops.gruppen2.domain.User;
import mops.gruppen2.domain.Visibility;
import mops.gruppen2.service.CsvService;
import mops.gruppen2.service.GroupService;
import mops.gruppen2.service.IdService;
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
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@Controller
@SessionScope
@RequestMapping("/gruppen2")
@Log4j2
public class GroupDetailsController {

    private final InviteService inviteService;
    private final GroupService groupService;
    private final ProjectionService projectionService;

    public GroupDetailsController(InviteService inviteService, GroupService groupService, ProjectionService projectionService) {
        this.inviteService = inviteService;
        this.groupService = groupService;
        this.projectionService = projectionService;
    }

    @RolesAllowed({"ROLE_orga", "ROLE_studentin", "ROLE_actuator"})
    @GetMapping("/details/{id}")
    public String showGroupDetails(KeycloakAuthenticationToken token,
                                   Model model,
                                   HttpServletRequest request,
                                   @PathVariable("id") String groupId) {

        log.info("GET to /details\n");

        Account account = new Account(token);
        User user = new User(account);

        model.addAttribute("account", account);

        Group group = projectionService.projectSingleGroup(UUID.fromString(groupId));
        model.addAttribute("group", group);

        // Parent Badge
        UUID parentId = group.getParent();
        Group parent = projectionService.projectParent(parentId);

        // Detailseite für private Gruppen
        if (!ValidationService.checkIfGroupAccess(group, user)) {
            return "detailsNoMember";
        }

        model.addAttribute("roles", group.getRoles());
        model.addAttribute("user", user);
        model.addAttribute("admin", Role.ADMIN);
        model.addAttribute("public", Visibility.PUBLIC);
        model.addAttribute("private", Visibility.PRIVATE);
        model.addAttribute("parent", parent);

        // Invitelink Anzeige für Admins
        if (ValidationService.checkIfAdmin(group, user)) {
            String actualURL = request.getRequestURL().toString();
            String serverURL = actualURL.substring(0, actualURL.indexOf("gruppen2/"));

            model.addAttribute("link", serverURL + "gruppen2/acceptinvite/" + inviteService.getLinkByGroup(group));
        }

        return "detailsMember";
    }

    @RolesAllowed({"ROLE_orga", "ROLE_studentin", "ROLE_actuator"})
    @GetMapping("/details/changeMetadata/{id}")
    public String changeMetadata(KeycloakAuthenticationToken token,
                                 Model model,
                                 @PathVariable("id") String groupId) {

        log.info("GET to /details/changeMetadata\n");

        Account account = new Account(token);
        User user = new User(account);

        Group group = projectionService.projectSingleGroup(UUID.fromString(groupId));
        ValidationService.throwIfNoAdmin(group, user);

        model.addAttribute("account", account);
        model.addAttribute("title", group.getTitle());
        model.addAttribute("description", group.getDescription());
        model.addAttribute("admin", Role.ADMIN);
        model.addAttribute("roles", group.getRoles());
        model.addAttribute("groupId", group.getId());
        model.addAttribute("user", user);

        return "changeMetadata";
    }

    @RolesAllowed({"ROLE_orga", "ROLE_studentin", "ROLE_actuator"})
    @PostMapping("/details/changeMetadata")
    @CacheEvict(value = "groups", allEntries = true)
    public String postChangeMetadata(KeycloakAuthenticationToken token,
                                     @RequestParam("title") String title,
                                     @RequestParam("description") String description,
                                     @RequestParam("groupId") String groupId) {

        log.info("POST to /details/changeMetadata\n");

        Account account = new Account(token);
        User user = new User(account);

        Group group = projectionService.projectSingleGroup(UUID.fromString(groupId));
        ValidationService.throwIfNoAdmin(group, user);
        groupService.updateTitle(user, group, title);
        groupService.updateDescription(user, group, description);

        return "redirect:/gruppen2/details/" + groupId;
    }

    @RolesAllowed({"ROLE_orga", "ROLE_studentin", "ROLE_actuator"})
    @GetMapping("/details/members/{id}")
    public String editMembers(KeycloakAuthenticationToken token,
                              Model model,
                              @PathVariable("id") String groupId) {

        log.info("GET to /details/members\n");

        Account account = new Account(token);
        User user = new User(account);

        Group group = projectionService.projectSingleGroup(UUID.fromString(groupId));
        ValidationService.throwIfNoAdmin(group, user);

        model.addAttribute("account", account);
        model.addAttribute("members", group.getMembers());
        model.addAttribute("group", group);
        model.addAttribute("admin", Role.ADMIN);

        return "editMembers";
    }

    @RolesAllowed({"ROLE_orga", "ROLE_studentin", "ROLE_actuator"})
    @PostMapping("/details/members/changeRole")
    @CacheEvict(value = "groups", allEntries = true)
    public String changeRole(KeycloakAuthenticationToken token,
                             @RequestParam("group_id") String groupId,
                             @RequestParam("user_id") String userId) {

        log.info("POST to /details/members/changeRole\n");

        Account account = new Account(token);
        User user = new User(account);

        Group group = projectionService.projectSingleGroup(UUID.fromString(groupId));
        ValidationService.throwIfNoAdmin(group, user);
        groupService.toggleMemberRole(new User(userId), group);

        // Falls sich der User selbst die Rechte genommen hat
        if (!ValidationService.checkIfAdmin(group, user)) {
            return "redirect:/gruppen2/details/" + groupId;
        }

        return "redirect:/gruppen2/details/members/" + groupId;
    }

    @RolesAllowed({"ROLE_orga", "ROLE_studentin", "ROLE_actuator"})
    @PostMapping("/details/members/changeMaximum")
    @CacheEvict(value = "groups", allEntries = true)
    public String changeMaxSize(KeycloakAuthenticationToken token,
                                @RequestParam("maximum") long userLimit,
                                @RequestParam("group_id") String groupId) {

        log.info("POST to /details/members/changeMaximum\n");

        Account account = new Account(token);
        User user = new User(account);

        Group group = projectionService.projectSingleGroup(UUID.fromString(groupId));
        groupService.updateUserLimit(user, group, userLimit);

        return "redirect:/gruppen2/details/members/" + groupId;
    }

    @RolesAllowed({"ROLE_orga", "ROLE_studentin", "ROLE_actuator"})
    @PostMapping("/details/members/deleteUser")
    @CacheEvict(value = "groups", allEntries = true)
    public String deleteUser(KeycloakAuthenticationToken token,
                             @RequestParam("group_id") String groupId,
                             @RequestParam("user_id") String userId) {

        log.info("POST to /details/members/deleteUser\n");

        Account account = new Account(token);
        User user = new User(account);

        // Der eingeloggte User kann sich nicht selbst entfernen
        if (!userId.equals(user.getId())) {
            Group group = projectionService.projectSingleGroup(UUID.fromString(groupId));
            groupService.deleteUser(new User(userId), group);
        }

        return "redirect:/gruppen2/details/members/" + groupId;
    }

    @RolesAllowed({"ROLE_orga", "ROLE_studentin", "ROLE_actuator"})
    @PostMapping("/detailsBeitreten")
    @CacheEvict(value = "groups", allEntries = true)
    public String joinGroup(KeycloakAuthenticationToken token,
                            Model model,
                            @RequestParam("id") String groupId) {

        log.info("POST to /detailsBeitreten\n");

        Account account = new Account(token);
        User user = new User(account);

        model.addAttribute("account", account);

        Group group = projectionService.projectSingleGroup(UUID.fromString(groupId));
        groupService.addUser(user, group);

        return "redirect:/gruppen2";
    }

    @RolesAllowed({"ROLE_orga", "ROLE_studentin", "ROLE_actuator"})
    @PostMapping("/leaveGroup")
    @CacheEvict(value = "groups", allEntries = true)
    public String leaveGroup(KeycloakAuthenticationToken token,
                             @RequestParam("group_id") String groupId) {

        log.info("POST to /leaveGroup\n");

        Account account = new Account(token);
        User user = new User(account);

        Group group = projectionService.projectSingleGroup(UUID.fromString(groupId));
        groupService.deleteUser(user, group);

        return "redirect:/gruppen2";
    }

    @RolesAllowed({"ROLE_orga", "ROLE_studentin", "ROLE_actuator"})
    @PostMapping("/deleteGroup")
    @CacheEvict(value = "groups", allEntries = true)
    public String deleteGroup(KeycloakAuthenticationToken token,
                              @RequestParam("group_id") String groupId) {

        log.info("POST to /deleteGroup\n");

        Account account = new Account(token);
        User user = new User(account);

        Group group = projectionService.projectSingleGroup(UUID.fromString(groupId));
        groupService.deleteGroup(user, group);

        return "redirect:/gruppen2";
    }

    @RolesAllowed({"ROLE_orga", "ROLE_actuator"})
    @PostMapping("/details/members/addUsersFromCsv")
    @CacheEvict(value = "groups", allEntries = true)
    public String addUsersFromCsv(KeycloakAuthenticationToken token,
                                  @RequestParam("group_id") String groupId,
                                  @RequestParam(value = "file", required = false) MultipartFile file) {

        log.info("POST to /details/members/addUsersFromCsv\n");

        Account account = new Account(token);
        User user = new User(account);

        Group group = projectionService.projectSingleGroup(IdService.stringToUUID(groupId));
        groupService.addUsersToGroup(CsvService.readCsvFile(file), group, user);

        return "redirect:/gruppen2/details/members/" + groupId;
    }
}
