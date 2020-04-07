package mops.gruppen2.domain;

public enum Role {
    ADMIN,
    MEMBER;

    public Role toggle() {
        return this == ADMIN ? MEMBER : ADMIN;
    }
}
