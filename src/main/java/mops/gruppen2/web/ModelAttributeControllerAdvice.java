package mops.gruppen2.web;

import mops.gruppen2.domain.Account;
import mops.gruppen2.domain.GroupType;
import mops.gruppen2.domain.Role;
import mops.gruppen2.domain.User;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class ModelAttributeControllerAdvice {

    // Add modelAttributes before each @RequestMapping
    @ModelAttribute
    public void modelAttributes(KeycloakAuthenticationToken token,
                                Model model) {

        // Prevent NullPointerException if not logged in
        if (token != null) {
            model.addAttribute("account", new Account(token));
            model.addAttribute("user", new User(token));
        }

        // Add enums
        model.addAttribute("member", Role.MEMBER);
        model.addAttribute("admin", Role.ADMIN);
        model.addAttribute("public", GroupType.PUBLIC);
        model.addAttribute("private", GroupType.PRIVATE);
        model.addAttribute("lecture", GroupType.LECTURE);
    }

}
