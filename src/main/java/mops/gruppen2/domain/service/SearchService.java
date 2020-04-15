package mops.gruppen2.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.exception.EventException;
import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.domain.model.group.SortHelper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class SearchService {

    private final ProjectionService projectionService;

    /**
     * Filtert alle öffentliche Gruppen nach dem Suchbegriff und gibt diese als sortierte Liste zurück.
     * Groß- und Kleinschreibung wird nicht beachtet.
     * Der Suchbegriff wird im Gruppentitel und in der Beschreibung gesucht.
     *
     * @param search Der Suchstring
     *
     * @return Liste von projizierten Gruppen
     *
     * @throws EventException Projektionsfehler
     */
    @Cacheable("groups")
    public List<Group> searchPublicGroups(String search, String principal) {
        List<Group> groups = projectionService.projectPublicGroups();
        System.out.println(groups);
        projectionService.removeUserGroups(groups, principal);
        System.out.println(groups);
        SortHelper.sortByGroupType(groups);

        if (search.isEmpty()) {
            return groups;
        }

        log.debug("Es wurde gesucht nach: {}", search);

        return groups.stream()
                     .filter(group -> group.format().toLowerCase().contains(search.toLowerCase()))
                     .collect(Collectors.toList());
    }

}
