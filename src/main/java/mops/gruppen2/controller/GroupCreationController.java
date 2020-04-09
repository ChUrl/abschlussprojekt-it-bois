package mops.gruppen2.controller;

import lombok.extern.log4j.Log4j2;
import mops.gruppen2.aspect.annotation.TraceMethodCalls;
import mops.gruppen2.domain.Group;
import mops.gruppen2.domain.GroupType;
import mops.gruppen2.domain.User;
import mops.gruppen2.service.CsvService;
import mops.gruppen2.service.GroupService;
import mops.gruppen2.service.IdService;
import mops.gruppen2.service.ProjectionService;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.security.RolesAllowed;

import static mops.gruppen2.service.ControllerService.getGroupType;
import static mops.gruppen2.service.ControllerService.getParent;
import static mops.gruppen2.service.ControllerService.getUserLimit;
import static mops.gruppen2.service.ControllerService.getVisibility;

@SuppressWarnings("SameReturnValue")
@Log4j2
@TraceMethodCalls
@Controller
@RequestMapping("/gruppen2")
public class GroupCreationController {

    private final GroupService groupService;
    private final ProjectionService projectionService;

    public GroupCreationController(GroupService groupService, ProjectionService projectionService) {
        this.groupService = groupService;
        this.projectionService = projectionService;
    }

    @RolesAllowed("ROLE_orga")
    @GetMapping("/create/orga")
    public String getCreateOrga(Model model) {

        model.addAttribute("lectures", projectionService.projectLectures());

        return "createOrga";
    }

    @RolesAllowed("ROLE_orga")
    @PostMapping("/create/orga")
    @CacheEvict(value = "groups", allEntries = true)
    public String postCreateOrga(KeycloakAuthenticationToken token,
                                 @RequestParam("title") String title,
                                 @RequestParam("description") String description,
                                 @RequestParam("visibility") boolean isPrivate,
                                 @RequestParam("lecture") boolean isLecture,
                                 @RequestParam("maxInfiniteUsers") boolean isInfinite,
                                 @RequestParam("userMaximum") long userLimit,
                                 @RequestParam("parent") String parent,
                                 @RequestParam(value = "file", required = false) MultipartFile file) {

        User user = new User(token);
        Group group = groupService.createGroup(user,
                                               title,
                                               description,
                                               getVisibility(isPrivate),
                                               getGroupType(isLecture),
                                               getUserLimit(isInfinite, userLimit),
                                               getParent(parent, isLecture));

        groupService.addUsersToGroup(CsvService.readCsvFile(file), group, user);

        return "redirect:/gruppen2/details/" + IdService.uuidToString(group.getId());
    }

    @RolesAllowed("ROLE_studentin")
    @GetMapping("/create/student")
    public String getCreateStudent(Model model) {

        model.addAttribute("lectures", projectionService.projectLectures());

        return "createStudent";
    }

    @RolesAllowed("ROLE_studentin")
    @PostMapping("/create/student")
    @CacheEvict(value = "groups", allEntries = true)
    public String postCreateStudent(KeycloakAuthenticationToken token,
                                    @RequestParam("title") String title,
                                    @RequestParam("description") String description,
                                    @RequestParam("visibility") boolean isPrivate,
                                    @RequestParam("maxInfiniteUsers") boolean isInfinite,
                                    @RequestParam("userMaximum") long userLimit,
                                    @RequestParam("parent") String parent) {

        User user = new User(token);
        Group group = groupService.createGroup(user,
                                               title,
                                               description,
                                               getVisibility(isPrivate),
                                               GroupType.SIMPLE,
                                               getUserLimit(isInfinite, userLimit),
                                               getParent(parent, false));

        return "redirect:/gruppen2/details/" + IdService.uuidToString(group.getId());
    }
}
