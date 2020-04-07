package mops.gruppen2.controller;

import mops.gruppen2.domain.Account;
import mops.gruppen2.domain.GroupType;
import mops.gruppen2.service.ControllerService;
import mops.gruppen2.service.GroupService;
import mops.gruppen2.service.IdService;
import mops.gruppen2.service.ProjectionService;
import mops.gruppen2.service.ValidationService;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.security.RolesAllowed;
import java.util.UUID;

import static mops.gruppen2.service.ControllerService.getGroupType;
import static mops.gruppen2.service.ControllerService.getUserMaximum;
import static mops.gruppen2.service.ControllerService.getVisibility;

@Controller
@SessionScope
@RequestMapping("/gruppen2")
public class GroupCreationController {

    private final GroupService groupService;
    private final ControllerService controllerService;
    private final ValidationService validationService;
    private final ProjectionService projectionService;

    public GroupCreationController(GroupService groupService, ControllerService controllerService, ValidationService validationService, ProjectionService projectionService) {
        this.groupService = groupService;
        this.controllerService = controllerService;
        this.validationService = validationService;
        this.projectionService = projectionService;
    }

    @RolesAllowed({"ROLE_orga", "ROLE_actuator"})
    @GetMapping("/createOrga")
    public String createGroupAsOrga(KeycloakAuthenticationToken token,
                                    Model model) {

        Account account = new Account(token);

        model.addAttribute("account", account);
        model.addAttribute("lectures", projectionService.projectLectures());

        return "createOrga";
    }

    @RolesAllowed({"ROLE_orga", "ROLE_actuator"})
    @PostMapping("/createOrga")
    @CacheEvict(value = "groups", allEntries = true)
    public String postCrateGroupAsOrga(KeycloakAuthenticationToken token,
                                       @RequestParam("title") String title,
                                       @RequestParam("description") String description,
                                       @RequestParam("visibility") boolean isPrivate,
                                       @RequestParam("lecture") boolean isLecture,
                                       @RequestParam("maxInfiniteUsers") boolean isInfinite,
                                       @RequestParam("userMaximum") long maxUsers,
                                       @RequestParam(value = "parent", required = false) String parent,
                                       @RequestParam(value = "file", required = false) MultipartFile file) {

        Account account = new Account(token);
        UUID parentUUID = IdService.stringToUUID(parent);

        validationService.checkFields(description, title, maxUsers, isInfinite);

        groupService.createGroupAsOrga(account,
                                       title,
                                       description,
                                       getVisibility(isPrivate),
                                       getGroupType(isLecture),
                                       getUserMaximum(isInfinite, maxUsers),
                                       parentUUID,
                                       file);

        return "redirect:/gruppen2";
    }

    @RolesAllowed("ROLE_studentin")
    @GetMapping("/createStudent")
    public String createGroupAsStudent(KeycloakAuthenticationToken token,
                                       Model model) {

        Account account = new Account(token);

        model.addAttribute("account", account);
        model.addAttribute("lectures", projectionService.projectLectures());

        return "createStudent";
    }

    @RolesAllowed("ROLE_studentin")
    @PostMapping("/createStudent")
    @CacheEvict(value = "groups", allEntries = true)
    public String postCreateGroupAsStudent(KeycloakAuthenticationToken token,
                                           @RequestParam("title") String title,
                                           @RequestParam("description") String description,
                                           @RequestParam("visibility") boolean isPrivate,
                                           @RequestParam("maxInfiniteUsers") boolean isInfinite,
                                           @RequestParam("userMaximum") long maxUsers,
                                           @RequestParam(value = "parent", required = false) String parent) {

        Account account = new Account(token);
        UUID parentUUID = IdService.stringToUUID(parent);

        validationService.checkFields(description, title, maxUsers, isInfinite);

        groupService.createGroup(account,
                                 title,
                                 description,
                                 getVisibility(isPrivate),
                                 GroupType.SIMPLE,
                                 getUserMaximum(isInfinite, maxUsers),
                                 parentUUID);

        return "redirect:/gruppen2";
    }
}
