package mops.gruppen2.domain.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;

@Getter
@AllArgsConstructor
@NoArgsConstructor // Für Jackson: CSV-Import
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class User {

    @EqualsAndHashCode.Include
    private String id;

    private String givenname;
    @ToString.Exclude
    private String familyname;
    @ToString.Exclude
    private String email;

    public User(KeycloakAuthenticationToken token) {
        KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
        id = principal.getName();
        givenname = principal.getKeycloakSecurityContext().getIdToken().getGivenName();
        familyname = principal.getKeycloakSecurityContext().getIdToken().getFamilyName();
        email = principal.getKeycloakSecurityContext().getIdToken().getEmail();
    }

    /**
     * User identifizieren sich über die Id, mehr wird also manchmal nicht benötigt.
     *
     * @param userId Die User Id
     */
    public User(String userId) {
        id = userId;
        givenname = "";
        familyname = "";
        email = "";
    }
}
