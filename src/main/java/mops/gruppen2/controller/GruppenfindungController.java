package mops.gruppen2.controller;

import mops.gruppen2.domain.Account;
import mops.gruppen2.domain.User;
import mops.gruppen2.domain.exception.PageNotFoundException;
import mops.gruppen2.service.ProjectionService;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

@Controller
public class GruppenfindungController {

    private final ProjectionService projectionService;

    public GruppenfindungController(ProjectionService projectionService) {
        this.projectionService = projectionService;
    }

    @GetMapping("")
    public String redirect() {
        return "redirect:/gruppen2";
    }

    @RolesAllowed({"ROLE_orga", "ROLE_studentin", "ROLE_actuator"})
    @GetMapping("/gruppen2")
    public String index(KeycloakAuthenticationToken token,
                        Model model) {

        Account account = new Account(token);
        User user = new User(account);

        model.addAttribute("account", account);
        model.addAttribute("gruppen", projectionService.projectUserGroups(user.getId()));
        model.addAttribute("user", user);

        return "index";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) throws ServletException {
        request.logout();
        return "redirect:/gruppen2/";
    }

    @GetMapping("/gruppen2/*")
    public String defaultLink() throws PageNotFoundException {
        throw new PageNotFoundException("\uD83D\uDE41");
    }
}
