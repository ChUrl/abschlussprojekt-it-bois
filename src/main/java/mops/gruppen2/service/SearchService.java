package mops.gruppen2.service;

import mops.gruppen2.domain.Account;
import mops.gruppen2.domain.Group;
import mops.gruppen2.domain.GroupType;
import mops.gruppen2.domain.exception.EventException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private final ProjectionService projectionService;

    public SearchService(ProjectionService projectionService) {this.projectionService = projectionService;}

    /**
     * Sortiert die übergebene Liste an Gruppen, sodass Veranstaltungen am Anfang der Liste sind.
     *
     * @param groups Die Liste von Gruppen die sortiert werden soll
     */
    //TODO: ProjectionService/SearchSortService
    static void sortByGroupType(List<Group> groups) {
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

    /**
     * Filtert alle öffentliche Gruppen nach dem Suchbegriff und gibt diese als Liste von Gruppen zurück.
     * Groß und Kleinschreibung wird nicht beachtet.
     *
     * @param search Der Suchstring
     *
     * @return Liste von projizierten Gruppen
     *
     * @throws EventException Projektionsfehler
     */
    //TODO: ProjectionService/SearchSortService
    //Todo Rename
    @Cacheable("groups")
    public List<Group> findGroupWith(String search, Account account) throws EventException {
        if (search.isEmpty()) {
            return projectionService.getAllGroupWithVisibilityPublic(account.getName());
        }

        return projectionService.getAllGroupWithVisibilityPublic(account.getName()).parallelStream().filter(group -> group.getTitle().toLowerCase().contains(search.toLowerCase()) || group.getDescription().toLowerCase().contains(search.toLowerCase())).collect(Collectors.toList());
    }
}
