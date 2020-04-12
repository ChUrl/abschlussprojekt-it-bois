package mops.gruppen2.domain.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;

@Value
@AllArgsConstructor
@ToString
public class User {

    @EqualsAndHashCode.Include
    String userid;

    String givenname;

    @ToString.Exclude
    String familyname;

    @ToString.Exclude
    String email;

    public User(KeycloakAuthenticationToken token) {
        KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
        userid = principal.getName();
        givenname = principal.getKeycloakSecurityContext().getIdToken().getGivenName();
        familyname = principal.getKeycloakSecurityContext().getIdToken().getFamilyName();
        email = principal.getKeycloakSecurityContext().getIdToken().getEmail();
    }

    /**
     * User identifizieren sich über die Id, mehr wird also manchmal nicht benötigt.
     *
     * @param userid Die User Id
     */
    public User(String userid) {
        this.userid = userid;
        givenname = "";
        familyname = "";
        email = "";
    }
}
