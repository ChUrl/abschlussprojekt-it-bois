package mops.gruppen2.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"givenname", "familyname", "email"})
public class User {

    private String id;
    private String givenname;
    private String familyname;
    private String email;

    public User(Account account) {
        id = account.getName();
        givenname = account.getGivenname();
        familyname = account.getFamilyname();
        email = account.getEmail();
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
