package mops.gruppen2.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

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
public class Group {

    @EqualsAndHashCode.Include
    private UUID id;
    private UUID parent;

    private GroupType type;
    private Visibility visibility;

    private String title;
    private String description;
    private Long userMaximum;

    //TODO: List to Hashmap
    private final List<User> members = new ArrayList<>();
    private final Map<String, Role> roles = new HashMap<>();

    @Override
    public String toString() {
        return title + ": " + description;
    }

}
