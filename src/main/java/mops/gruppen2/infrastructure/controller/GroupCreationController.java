package mops.gruppen2.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.aspect.annotation.TraceMethodCalls;
import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.domain.model.group.Type;
import mops.gruppen2.domain.model.group.User;
import mops.gruppen2.domain.model.group.wrapper.Description;
import mops.gruppen2.domain.model.group.wrapper.Limit;
import mops.gruppen2.domain.model.group.wrapper.Parent;
import mops.gruppen2.domain.model.group.wrapper.Title;
import mops.gruppen2.domain.service.GroupService;
import mops.gruppen2.domain.service.helper.CsvHelper;
import mops.gruppen2.domain.service.helper.ProjectionHelper;
import mops.gruppen2.domain.service.helper.ValidationHelper;
import mops.gruppen2.infrastructure.GroupCache;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;

@SuppressWarnings("SameReturnValue")
@Log4j2
@TraceMethodCalls
@RequiredArgsConstructor
@Controller
@RequestMapping("/gruppen2")
public class GroupCreationController {

    private final GroupCache groupCache;

    private final GroupService groupService;
    private final ProjectionHelper projectionHelper;

    @RolesAllowed({"ROLE_orga", "ROLE_studentin"})
    @GetMapping("/create")
    public String getCreate(Model model) {

        model.addAttribute("lectures", groupCache.lectures());

        return "create";
    }

    @RolesAllowed({"ROLE_orga", "ROLE_studentin"})
    @PostMapping("/create")
    public String postCreateOrga(KeycloakAuthenticationToken token,
                                 @RequestParam("type") Type type,
                                 @RequestParam("parent") @Valid Parent parent,
                                 @RequestParam("title") @Valid Title title,
                                 @RequestParam("description") @Valid Description description,
                                 @RequestParam("limit") @Valid Limit limit,
                                 @RequestParam(value = "file", required = false) MultipartFile file) {

        // Zusätzlicher check: studentin kann keine lecture erstellen
        ValidationHelper.validateCreateForm(token, type);

        String principal = token.getName();
        Group group = groupService.createGroup(principal);
        groupService.initGroupMembers(group, principal, principal, new User(token), limit);
        groupService.initGroupMeta(group, principal, type, parent);
        groupService.initGroupText(group, principal, title, description);

        // ROLE_studentin kann kein CSV importieren
        if (token.getAccount().getRoles().contains("orga")) {
            groupService.addUsersToGroup(group, principal, CsvHelper.readCsvFile(file));
        }

        return "redirect:/gruppen2/details/" + group.getId();
    }
}
