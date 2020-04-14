package mops.gruppen2.web.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mops.gruppen2.domain.model.group.Group;

import java.util.List;

/**
 * Kombiniert den Status und die Gruppenliste zur ausgabe über die API.
 */
@AllArgsConstructor
@Getter
public class GroupRequestWrapper {

    private final long status;
    private final List<Group> groupList;
}
