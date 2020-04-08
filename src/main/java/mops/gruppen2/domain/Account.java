package mops.gruppen2.domain;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;

import java.util.Set;

@Value
@AllArgsConstructor
public class Account {

    String name; //user_id
    String email;
    String image;
    String givenname;
    String familyname;
    Set<String> roles;

    public Account(KeycloakAuthenticationToken token) {
        KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
        name = principal.getName();
        email = principal.getKeycloakSecurityContext().getIdToken().getEmail();
        image = null;
        givenname = principal.getKeycloakSecurityContext().getIdToken().getGivenName();
        familyname = principal.getKeycloakSecurityContext().getIdToken().getFamilyName();
        roles = token.getAccount().getRoles();
    }
}
