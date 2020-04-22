package mops.gruppen2.domain.service;

import mops.gruppen2.GroupBuilder;
import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.domain.model.group.Type;
import mops.gruppen2.infrastructure.GroupCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SearchServiceTest {

    private GroupCache groupCache;
    private SearchService searchService;

    @BeforeEach
    void setUp() {
        groupCache = new GroupCache(mock(EventStoreService.class));
        searchService = new SearchService(groupCache);
    }

    @Test
    void searchString_noResult() {
        assertThat(searchService.searchString("", "TEST")).isEmpty();
    }

    @Test
    void searchString_noResult_onePrivate_emptyString() {
        GroupBuilder.get(groupCache, 1).group();

        assertThat(searchService.searchString("", "TEST")).isEmpty();
    }

    @Test
    void searchString_noResult_onePublic_emptyString_principalMember() {
        GroupBuilder.get(groupCache, 1).group().testadmin().publik();

        assertThat(searchService.searchString("", "TEST")).isEmpty();
    }

    @Test
    void searchString_oneResult_onePublic_emptyString_principalNoMember() {
        GroupBuilder.get(groupCache, 1).group().testadmin().publik();

        assertThat(searchService.searchString("", "PETER")).hasSize(1);
    }

    @Test
    void searchString_oneResult_multiple_emptyString() {
        Group groupA = GroupBuilder.get(groupCache, 1).group().testadmin().lecture().build();
        GroupBuilder.get(groupCache, 2).group().testadmin().publik().limit(2).add("PETER");
        GroupBuilder.get(groupCache, 3).group().testadmin().publik().limit(2).add("PETER");
        GroupBuilder.get(groupCache, 4).group().testadmin().privat();

        assertThat(searchService.searchString("", "PETER")).containsExactly(groupA);
        assertThat(searchService.searchString("", "TEST")).isEmpty();
    }

    @Test
    void searchString_noPrivates() {
        Group groupA = GroupBuilder.get(groupCache, 1).group().testadmin().lecture().build();
        Group groupB = GroupBuilder.get(groupCache, 2).group().testadmin().publik().limit(2).add("PETER").build();
        Group groupC = GroupBuilder.get(groupCache, 3).group().testadmin().publik().limit(2).add("PETER").build();
        GroupBuilder.get(groupCache, 4).group().testadmin().privat();
        GroupBuilder.get(groupCache, 5).group().testadmin().privat();
        GroupBuilder.get(groupCache, 6).group().testadmin().privat();

        assertThat(searchService.searchString("", "PETER")).containsExactly(groupA);
        assertThat(searchService.searchString("", "PRRR")).containsOnly(groupA, groupB, groupC);
        assertThat(searchService.searchString("", "TEST")).isEmpty();
    }

    @Test
    void searchString_matchString_title() {
        Group groupA = GroupBuilder.get(groupCache, 1).group().testadmin().lecture().title("A").build();
        Group groupB = GroupBuilder.get(groupCache, 2).group().testadmin().lecture().title("B").build();
        Group groupC = GroupBuilder.get(groupCache, 3).group().testadmin().lecture().title("C").build();
        Group groupD = GroupBuilder.get(groupCache, 4).group().testadmin().lecture().title("CAESAR").build();

        assertThat(searchService.searchString("C", "PETER")).containsExactly(groupC, groupD);
        assertThat(searchService.searchString("C", "TEST")).isEmpty();
    }

    @Test
    void searchString_matchString_desc() {
        Group groupA = GroupBuilder.get(groupCache, 1).group().testadmin().lecture().desc("A").build();
        Group groupB = GroupBuilder.get(groupCache, 2).group().testadmin().lecture().desc("B").build();
        Group groupC = GroupBuilder.get(groupCache, 3).group().testadmin().lecture().desc("C").build();
        Group groupD = GroupBuilder.get(groupCache, 4).group().testadmin().lecture().desc("CAESAR").build();

        assertThat(searchService.searchString("C", "PETER")).containsExactly(groupC, groupD);
        assertThat(searchService.searchString("C", "TEST")).isEmpty();
    }

    @Test
    void searchType_noGroup() {
        assertThat(searchService.searchType(Type.LECTURE, "PETER")).isEmpty();
        assertThat(searchService.searchType(Type.PUBLIC, "PETER")).isEmpty();
        assertThat(searchService.searchType(Type.PRIVATE, "PETER")).isEmpty();
    }

    @Test
    void searchType_noPrivates() {
        GroupBuilder.get(groupCache, 1).group().testadmin().lecture();
        GroupBuilder.get(groupCache, 2).group().testadmin().publik();
        GroupBuilder.get(groupCache, 3).group().testadmin().privat();
        GroupBuilder.get(groupCache, 4).group().testadmin().privat();
        GroupBuilder.get(groupCache, 5).group().testadmin().lecture();

        assertThat(searchService.searchType(Type.LECTURE, "PETER")).hasSize(2);
        assertThat(searchService.searchType(Type.PUBLIC, "PETER")).hasSize(1);
        assertThat(searchService.searchType(Type.PRIVATE, "PETER")).isEmpty();
    }
}
