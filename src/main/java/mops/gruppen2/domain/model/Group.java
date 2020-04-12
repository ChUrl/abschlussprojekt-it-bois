package mops.gruppen2.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Repräsentiert den aggregierten Zustand einer Gruppe.
 */
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Group {

    @EqualsAndHashCode.Include
    private UUID id;

    @ToString.Exclude
    private UUID parent;

    private Type type;

    private Title title;
    private Description description;

    @ToString.Exclude
    private Limit userLimit = new Limit(1); // Add initial user

    @ToString.Exclude
    private final Map<String, User> members = new HashMap<>();
    @ToString.Exclude
    private final Map<String, Role> roles = new HashMap<>();
}
