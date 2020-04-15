package mops.gruppen2.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.exception.EventException;
import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.infrastructure.GroupCache;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class SearchService {

    private final GroupCache groupCache;

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
    //TODO: search in lectures
    @Cacheable("groups")
    public List<Group> searchPublicGroups(String search, String principal) {
        List<Group> groups = groupCache.publics();

        groups = removeUserGroups(groups, principal);

        if (search.isEmpty()) {
            return groups;
        }

        log.debug("Es wurde gesucht nach: {}", search);

        return groups.stream()
                     .filter(group -> group.format().toLowerCase().contains(search.toLowerCase()))
                     .collect(Collectors.toList());
    }

    private static List<Group> removeUserGroups(List<Group> groups, String principal) {
        return groups.stream()
                     .filter(group -> !group.isMember(principal))
                     .collect(Collectors.toList());
    }

}
