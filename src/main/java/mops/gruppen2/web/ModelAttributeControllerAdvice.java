package mops.gruppen2.web;

import mops.gruppen2.domain.Account;
import mops.gruppen2.domain.model.Role;
import mops.gruppen2.domain.model.Type;
import mops.gruppen2.domain.model.User;
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
        model.addAttribute("public", Type.PUBLIC);
        model.addAttribute("private", Type.PRIVATE);
        model.addAttribute("lecture", Type.LECTURE);
    }

}
