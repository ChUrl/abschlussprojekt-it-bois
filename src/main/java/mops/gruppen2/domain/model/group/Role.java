package mops.gruppen2.domain.model.group;

public enum Role {
    ADMIN,
    REGULAR;

    public Role toggle() {
        return this == ADMIN ? REGULAR : ADMIN;
    }
}
