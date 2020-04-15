package mops.gruppen2.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.aspect.annotation.TraceMethodCall;
import mops.gruppen2.domain.exception.PageNotFoundException;
import mops.gruppen2.infrastructure.GroupCache;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

@SuppressWarnings("SameReturnValue")
@Log4j2
@RequiredArgsConstructor
@Controller
public class GruppenfindungController {

    private final GroupCache groupCache;

    // For convenience
    @GetMapping("")
    public String redirect() {
        return "redirect:/gruppen2";
    }

    @GetMapping("/login")
    public String login() {
        return "redirect:/gruppen2";
    }

    @TraceMethodCall
    @RolesAllowed({"ROLE_orga", "ROLE_studentin"})
    @GetMapping("/gruppen2")
    public String getIndexPage(KeycloakAuthenticationToken token,
                               Model model) {
        model.addAttribute("groups", groupCache.userGroups(token.getName()));

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
