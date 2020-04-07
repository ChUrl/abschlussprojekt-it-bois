package mops.gruppen2.service;

import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.Group;
import mops.gruppen2.domain.GroupType;
import mops.gruppen2.domain.exception.EventException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
public class SearchService {

    private final ProjectionService projectionService;

    public SearchService(ProjectionService projectionService) {
        this.projectionService = projectionService;
    }

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
    public List<Group> searchPublicGroups(String search, String userId) throws EventException {
        List<Group> groups = projectionService.projectPublicGroups();
        projectionService.removeUserGroups(groups, userId);
        sortByGroupType(groups);

        if (search.isEmpty()) {
            return groups;
        }

        log.trace("Es wurde gesucht nach: {}", search);

        return groups.stream()
                     .filter(group -> group.toString().toLowerCase().contains(search.toLowerCase()))
                     .collect(Collectors.toList());
    }

    /**
     * Sortiert die übergebene Liste an Gruppen, sodass Veranstaltungen am Anfang der Liste sind.
     *
     * @param groups Die Liste von Gruppen die sortiert werden soll
     */
    private static void sortByGroupType(List<Group> groups) {
        groups.sort((Group g1, Group g2) -> {
            if (g1.getType() == GroupType.LECTURE) {
                return -1;
            }
            if (g2.getType() == GroupType.LECTURE) {
                return 0;
            }

            return 1;
        });
    }
}
