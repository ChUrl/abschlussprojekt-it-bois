package mops.gruppen2.web;

import lombok.extern.log4j.Log4j2;
import mops.gruppen2.aspect.annotation.TraceMethodCalls;
import mops.gruppen2.domain.Group;
import mops.gruppen2.domain.User;
import mops.gruppen2.domain.service.CsvService;
import mops.gruppen2.domain.service.GroupService;
import mops.gruppen2.domain.service.IdService;
import mops.gruppen2.domain.service.ProjectionService;
import mops.gruppen2.domain.service.ValidationService;
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
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import static mops.gruppen2.domain.service.ControllerService.getGroupType;
import static mops.gruppen2.domain.service.ControllerService.getParent;

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

    @RolesAllowed({"ROLE_orga", "ROLE_studentin"})
    @GetMapping("/create")
    public String getCreate(Model model) {

        model.addAttribute("lectures", projectionService.projectLectures());

        return "create";
    }

    @RolesAllowed({"ROLE_orga", "ROLE_studentin"})
    @PostMapping("/create")
    @CacheEvict(value = "groups", allEntries = true)
    public String postCreateOrga(KeycloakAuthenticationToken token,
                                 @NotBlank @RequestParam("title") String title,
                                 @NotBlank @RequestParam("description") String description,
                                 @NotBlank @RequestParam("type") String type,
                                 @Min(1) @Max(100_000) @RequestParam("userlimit") long userLimit,
                                 @RequestParam(value = "parent", defaultValue = "") String parent,
                                 @RequestParam(value = "file", required = false) MultipartFile file) {

        ValidationService.validateGroupType(token, type);

        User user = new User(token);
        Group group = groupService.createGroup(user,
                                               title,
                                               description,
                                               getGroupType(type),
                                               userLimit,
                                               getParent(parent, type));

        // ROLE_studentin kann kein CSV importieren
        if (token.getAccount().getRoles().contains("orga")) {
            groupService.addUsersToGroup(CsvService.readCsvFile(file), group, user);
        }

        return "redirect:/gruppen2/details/" + IdService.uuidToString(group.getId());
    }
}
