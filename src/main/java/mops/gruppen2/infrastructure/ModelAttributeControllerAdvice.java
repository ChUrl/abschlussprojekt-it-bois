package mops.gruppen2.infrastructure;

import mops.gruppen2.domain.Account;
import mops.gruppen2.domain.model.group.Role;
import mops.gruppen2.domain.model.group.Type;
import mops.gruppen2.domain.model.group.User;
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
            model.addAttribute("principal", new User(token));
        }

        // Add enums
        model.addAttribute("REGULAR", Role.REGULAR);
        model.addAttribute("ADMIN", Role.ADMIN);
        model.addAttribute("PUBLIC", Type.PUBLIC);
        model.addAttribute("PRIVATE", Type.PRIVATE);
        model.addAttribute("LECTURE", Type.LECTURE);
    }

}
