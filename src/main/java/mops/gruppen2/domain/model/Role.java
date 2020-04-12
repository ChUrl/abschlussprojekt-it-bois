package mops.gruppen2.domain.model;

public enum Role {
    ADMIN,
    MEMBER;

    public Role toggle() {
        return this == ADMIN ? MEMBER : ADMIN;
    }
}
