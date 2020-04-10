package mops.gruppen2.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    //TODO: Single Type for Public/Private/Lecture?
    private GroupType type;

    private String title;
    private String description;

    // Default + Minimum: 1
    @ToString.Exclude
    private long userLimit = 1;

    //TODO: List to Hashmap
    @ToString.Exclude
    private final List<User> members = new ArrayList<>();
    @ToString.Exclude
    private final Map<String, Role> roles = new HashMap<>();
}
