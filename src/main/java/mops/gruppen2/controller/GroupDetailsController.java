package mops.gruppen2.controller;

import lombok.extern.log4j.Log4j2;
import mops.gruppen2.aspect.annotation.TraceMethodCalls;
import mops.gruppen2.domain.Group;
import mops.gruppen2.domain.User;
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
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@SuppressWarnings("SameReturnValue")
@Log4j2
@TraceMethodCalls
@Controller
@RequestMapping("/gruppen2")
public class GroupDetailsController {

    private final InviteService inviteService;
    private final GroupService groupService;
    private final ProjectionService projectionService;

    public GroupDetailsController(InviteService inviteService, GroupService groupService, ProjectionService projectionService) {
        this.inviteService = inviteService;
        this.groupService = groupService;
        this.projectionService = projectionService;
    }

    @RolesAllowed({"ROLE_orga", "ROLE_studentin"})
    @GetMapping("/details/{id}")
    public String getDetailsPage(KeycloakAuthenticationToken token,
                                 Model model,
                                 HttpServletRequest request,
                                 @PathVariable("id") String groupId) {

        User user = new User(token);
        Group group = projectionService.projectSingleGroup(UUID.fromString(groupId));

        // Parent Badge
        UUID parentId = group.getParent();
        Group parent = projectionService.projectParent(parentId);

        // Invite Link
        String actualURL = request.getRequestURL().toString();
        String serverURL = actualURL.substring(0, actualURL.indexOf("gruppen2/"));
        String link = serverURL + "gruppen2/join/" + inviteService.getLinkByGroup(group);

        model.addAttribute("group", group);
        model.addAttribute("parent", parent);
        model.addAttribute("link", link);

        // Detailseite für nicht-Mitglieder
        if (!ValidationService.checkIfMember(group, user)) {
            return "detailsNoMember";
        }

        return "detailsMember";
    }

    @RolesAllowed({"ROLE_orga", "ROLE_studentin"})
    @PostMapping("/details/{id}/join")
    @CacheEvict(value = "groups", allEntries = true)
    public String postDetailsJoin(KeycloakAuthenticationToken token,
                                  @PathVariable("id") String groupId) {

        User user = new User(token);
        Group group = projectionService.projectSingleGroup(UUID.fromString(groupId));

        groupService.addUser(user, group);

        return "redirect:/gruppen2/details/" + groupId;
    }

    @RolesAllowed({"ROLE_orga", "ROLE_studentin"})
    @PostMapping("/details/{id}/leave")
    @CacheEvict(value = "groups", allEntries = true)
    public String postDetailsLeave(KeycloakAuthenticationToken token,
                                   @PathVariable("id") String groupId) {

        User user = new User(token);
        Group group = projectionService.projectSingleGroup(UUID.fromString(groupId));

        groupService.deleteUser(user, group);

        return "redirect:/gruppen2";
    }

    @RolesAllowed({"ROLE_orga", "ROLE_studentin"})
    @PostMapping("/details/{id}/destroy")
    @CacheEvict(value = "groups", allEntries = true)
    public String postDetailsDestroy(KeycloakAuthenticationToken token,
                                     @PathVariable("id") String groupId) {

        User user = new User(token);
        Group group = projectionService.projectSingleGroup(UUID.fromString(groupId));

        groupService.deleteGroup(user, group);

        return "redirect:/gruppen2";
    }

    @RolesAllowed({"ROLE_orga", "ROLE_studentin"})
    @GetMapping("/details/{id}/meta")
    public String getDetailsMeta(KeycloakAuthenticationToken token,
                                 Model model,
                                 @PathVariable("id") String groupId) {

        User user = new User(token);
        Group group = projectionService.projectSingleGroup(UUID.fromString(groupId));

        ValidationService.throwIfNoAdmin(group, user);

        model.addAttribute("group", group);

        return "changeMetadata";
    }

    @RolesAllowed({"ROLE_orga", "ROLE_studentin"})
    @PostMapping("/details/{id}/meta/update")
    @CacheEvict(value = "groups", allEntries = true)
    public String postDetailsMetaUpdate(KeycloakAuthenticationToken token,
                                        @PathVariable("id") String groupId,
                                        @RequestParam("title") String title,
                                        @RequestParam("description") String description) {

        User user = new User(token);
        Group group = projectionService.projectSingleGroup(UUID.fromString(groupId));

        groupService.updateTitle(user, group, title);
        groupService.updateDescription(user, group, description);

        return "redirect:/gruppen2/details/" + groupId;
    }

    @RolesAllowed({"ROLE_orga", "ROLE_studentin"})
    @GetMapping("/details/{id}/members")
    public String getDetailsMembers(KeycloakAuthenticationToken token,
                                    Model model,
                                    @PathVariable("id") String groupId) {

        User user = new User(token);
        Group group = projectionService.projectSingleGroup(UUID.fromString(groupId));

        ValidationService.throwIfNoAdmin(group, user);

        model.addAttribute("group", group);

        return "editMembers";
    }

    @RolesAllowed({"ROLE_orga", "ROLE_studentin"})
    @PostMapping("/details/{id}/members/update/userlimit")
    @CacheEvict(value = "groups", allEntries = true)
    public String postDetailsMembersUpdateUserLimit(KeycloakAuthenticationToken token,
                                                    @PathVariable("id") String groupId,
                                                    @RequestParam("maximum") long userLimit) {

        User user = new User(token);
        Group group = projectionService.projectSingleGroup(UUID.fromString(groupId));

        groupService.updateUserLimit(user, group, userLimit);

        return "redirect:/gruppen2/details/" + groupId + "/members";
    }

    @RolesAllowed("ROLE_orga")
    @PostMapping("/details/{id}/members/update/csv")
    @CacheEvict(value = "groups", allEntries = true)
    public String postDetailsMembersUpdateCsv(KeycloakAuthenticationToken token,
                                              @PathVariable("id") String groupId,
                                              @RequestParam(value = "file", required = false) MultipartFile file) {

        User user = new User(token);
        Group group = projectionService.projectSingleGroup(IdService.stringToUUID(groupId));

        groupService.addUsersToGroup(CsvService.readCsvFile(file), group, user);

        return "redirect:/gruppen2/details/" + groupId + "/members";
    }

    //TODO: Method + view for /details/{id}/members/{id}

    @RolesAllowed({"ROLE_orga", "ROLE_studentin"})
    @PostMapping("/details/{id}/members/{userid}/update/role")
    @CacheEvict(value = "groups", allEntries = true)
    public String postDetailsMembersUpdateRole(KeycloakAuthenticationToken token,
                                               @PathVariable("id") String groupId,
                                               @PathVariable("userid") String userId) {

        User user = new User(token);
        Group group = projectionService.projectSingleGroup(UUID.fromString(groupId));

        ValidationService.throwIfNoAdmin(group, user);

        groupService.toggleMemberRole(new User(userId), group);

        // Falls sich der User selbst die Rechte genommen hat
        if (!ValidationService.checkIfAdmin(group, user)) {
            return "redirect:/gruppen2/details/" + groupId;
        }

        return "redirect:/gruppen2/details/" + groupId + "/members";
    }

    @RolesAllowed({"ROLE_orga", "ROLE_studentin"})
    @PostMapping("/details/{id}/members/{userid}/delete")
    @CacheEvict(value = "groups", allEntries = true)
    public String postDetailsMembersDelete(KeycloakAuthenticationToken token,
                                           @PathVariable("id") String groupId,
                                           @PathVariable("userid") String userId) {

        User user = new User(token);
        Group group = projectionService.projectSingleGroup(UUID.fromString(groupId));

        ValidationService.throwIfNoAdmin(group, user);

        // Der eingeloggte User kann sich nicht selbst entfernen (er kann aber verlassen)
        if (!userId.equals(user.getId())) {
            groupService.deleteUser(new User(userId), group);
        }

        return "redirect:/gruppen2/details/" + groupId + "/members";
    }
}
