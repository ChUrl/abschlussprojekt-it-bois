package mops.gruppen2.infrastructure.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Kombiniert den Status und die Gruppenliste zur ausgabe über die API.
 */
@Getter
@AllArgsConstructor
public class GroupRequestWrapper {

    private final long version;
    private final List<GroupWrapper> groups;
}
