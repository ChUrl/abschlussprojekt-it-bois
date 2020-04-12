package mops.gruppen2.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.aspect.annotation.TraceMethodCalls;
import mops.gruppen2.domain.helper.CsvHelper;
import mops.gruppen2.domain.helper.IdHelper;
import mops.gruppen2.domain.helper.ValidationHelper;
import mops.gruppen2.domain.model.Description;
import mops.gruppen2.domain.model.Group;
import mops.gruppen2.domain.model.Limit;
import mops.gruppen2.domain.model.Title;
import mops.gruppen2.domain.model.User;
import mops.gruppen2.domain.service.GroupService;
import mops.gruppen2.domain.service.ProjectionService;
import mops.gruppen2.web.form.CreateForm;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;

@SuppressWarnings("SameReturnValue")
@Log4j2
@TraceMethodCalls
@RequiredArgsConstructor
@Controller
@RequestMapping("/gruppen2")
public class GroupCreationController {

    private final GroupService groupService;
    private final ProjectionService projectionService;

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
                                 @Valid CreateForm create,
                                 @Valid Title title,
                                 @Valid Description description,
                                 @Valid Limit limit) {

        // Zusätzlicher check: studentin kann keine lecture erstellen
        ValidationHelper.validateCreateForm(token, create);

        User user = new User(token);
        Group group = groupService.createGroup(user,
                                               title,
                                               description,
                                               create.getType(),
                                               limit,
                                               create.getParent());

        // ROLE_studentin kann kein CSV importieren
        if (token.getAccount().getRoles().contains("orga")) {
            groupService.addUsersToGroup(CsvHelper.readCsvFile(create.getFile()), group, user);
        }

        return "redirect:/gruppen2/details/" + IdHelper.uuidToString(group.getId());
    }
}
