package mops.gruppen2.domain.model.group;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;

@Log4j2
@Value
@AllArgsConstructor
public class User {

    @EqualsAndHashCode.Include
    @Getter(AccessLevel.NONE)
    @JsonProperty("id")
    String userid;

    @JsonProperty("givenname")
    String givenname;

    @JsonProperty("familyname")
    String familyname;

    @JsonProperty("email")
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

    public String getId() {
        return userid;
    }

    public String format() {
        return givenname + " " + familyname;
    }

    public boolean isMember(Group group) {
        return group.getMembers().contains(this);
    }
}
