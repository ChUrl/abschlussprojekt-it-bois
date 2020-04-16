package mops.gruppen2.infrastructure.api;

import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Kombiniert den Status und die Gruppenliste zur ausgabe über die API.
 */
@AllArgsConstructor
public class GroupRequestWrapper {

    private final long version;
    private final List<GroupWrapper> groups;
}
