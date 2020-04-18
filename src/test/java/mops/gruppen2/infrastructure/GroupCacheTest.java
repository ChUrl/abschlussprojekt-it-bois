package mops.gruppen2.infrastructure;

import mops.gruppen2.GroupBuilder;
import mops.gruppen2.domain.exception.GroupNotFoundException;
import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.domain.service.EventStoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static mops.gruppen2.TestHelper.uuid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Kann nur indirket über events getestet werden, diese werden also "mitgetestet"
class GroupCacheTest {

    private GroupCache cache;

    @BeforeEach
    void setUp() {
        cache = new GroupCache(Mockito.mock(EventStoreService.class));
    }

    @Test
    void groups_noGroups() {
        assertThat(cache.groups()).isEmpty();
    }

    @Test
    void group_groupNotFound() {
        assertThatThrownBy(() -> cache.group(uuid(1)))
                .isInstanceOf(GroupNotFoundException.class);
    }

    @Test
    void group_linkNotFound() {
        assertThatThrownBy(() -> cache.group("00000000-0000-0000-0000-000000000000"))
                .isInstanceOf(GroupNotFoundException.class);
    }

    @Test
    void group_groupFound() {
        Group group = GroupBuilder.get(cache, 1).group().build();

        assertThat(cache.group(uuid(1))).isEqualTo(group);
    }

    @Test
    void group_linkFound() {
        Group group = GroupBuilder.get(cache, 1).group().build();

        assertThat(cache.group(group.getLink())).isEqualTo(group);
    }

    @Test
    void userGroups_noGroups() {
        assertThat(cache.userGroups("TEST")).isEmpty();
    }

    @Test
    void userGroups_noUserGroups() {
        Group group = GroupBuilder.get(cache, 1).group().add("PETER").build();

        assertThat(cache.groups()).hasSize(1);
        assertThat(cache.userGroups("TEST")).isEmpty();
    }

    @Test
    void userGroups_oneUserGroup() {
        Group group = GroupBuilder.get(cache, 1).group().add("TEST").build();

        assertThat(cache.groups()).hasSize(1);
        assertThat(cache.userGroups("TEST")).containsExactly(group);
    }

    @Test
    void userGroups_userGroup_multiple() {
        Group groupA = GroupBuilder.get(cache, 1).group().add("TEST").build();
        Group groupB = GroupBuilder.get(cache, 2).group().add("PETER").build();
        Group groupC = GroupBuilder.get(cache, 3).group().add("TEST").build();
        Group groupD = GroupBuilder.get(cache, 4).group().add("PETER").build();

        assertThat(cache.groups()).hasSize(4);
        assertThat(cache.userGroups("PETER")).containsExactly(groupB, groupD);
    }

    @Test
    void userLectures_noGroups() {
        assertThat(cache.userLectures("PETER")).isEmpty();
    }

    @Test
    void userLectures_noLecture() {
        Group group = GroupBuilder.get(cache, 1).group().testadmin().publik().build();

        assertThat(cache.groups()).hasSize(1);
        assertThat(cache.userLectures("PETER")).isEmpty();
    }

    @Test
    void userLectures_oneLecture() {
        Group group = GroupBuilder.get(cache, 1).group().testadmin().limit(2).add("PETER").lecture().build();

        assertThat(cache.groups()).hasSize(1);
        assertThat(cache.userLectures("PETER")).containsExactly(group);
    }

    @Test
    void userLectures_lecture_multiple() {
        Group groupA = GroupBuilder.get(cache, 1).group().testadmin().limit(2).add("PETER").lecture().build();
        Group groupB = GroupBuilder.get(cache, 2).group().testadmin().limit(2).add("PETER").publik().build();
        Group groupC = GroupBuilder.get(cache, 3).group().testadmin().limit(2).add("PETER").privat().build();
        Group groupD = GroupBuilder.get(cache, 4).group().testadmin().lecture().build();

        assertThat(cache.groups()).hasSize(4);
        assertThat(cache.userLectures("PETER")).containsExactly(groupA);
    }

    @Test
    void userPublics_noGroups() {
        assertThat(cache.userPublics("PETER")).isEmpty();
    }

    @Test
    void userPublics_noPublic() {
        Group group = GroupBuilder.get(cache, 1).group().testadmin().limit(2).add("PETER").publik().build();

        assertThat(cache.groups()).hasSize(1);
        assertThat(cache.userLectures("PETER")).isEmpty();
    }

    @Test
    void userPublics_onePublic() {
        Group group = GroupBuilder.get(cache, 1).group().testadmin().limit(2).add("PETER").lecture().build();

        assertThat(cache.groups()).hasSize(1);
        assertThat(cache.userLectures("PETER")).containsExactly(group);
    }

    @Test
    void userPublics_public_multiple() {
        Group groupA = GroupBuilder.get(cache, 1).group().testadmin().limit(2).add("PETER").lecture().build();
        Group groupB = GroupBuilder.get(cache, 2).group().testadmin().limit(2).add("PETER").publik().build();
        Group groupC = GroupBuilder.get(cache, 3).group().testadmin().limit(2).add("PETER").privat().build();
        Group groupD = GroupBuilder.get(cache, 4).group().testadmin().publik().build();

        assertThat(cache.groups()).hasSize(4);
        assertThat(cache.userPublics("PETER")).containsExactly(groupB);
    }

    @Test
    void userPrivates_noGroups() {
        assertThat(cache.userPrivates("PETER")).isEmpty();
    }

    @Test
    void userPrivates_noPrivate() {
        Group group = GroupBuilder.get(cache, 1).group().testadmin().limit(2).add("PETER").publik().build();

        assertThat(cache.groups()).hasSize(1);
        assertThat(cache.userPrivates("PETER")).isEmpty();
    }

    @Test
    void userPrivates_onePrivate() {
        Group group = GroupBuilder.get(cache, 1).group().testadmin().limit(2).add("PETER").privat().build();

        assertThat(cache.groups()).hasSize(1);
        assertThat(cache.userPrivates("PETER")).containsExactly(group);
    }

    @Test
    void userPrivates_private_multiple() {
        Group groupA = GroupBuilder.get(cache, 1).group().testadmin().limit(2).add("PETER").lecture().build();
        Group groupB = GroupBuilder.get(cache, 2).group().testadmin().limit(2).add("PETER").privat().build();
        Group groupC = GroupBuilder.get(cache, 3).group().testadmin().privat().build();
        Group groupD = GroupBuilder.get(cache, 4).group().testadmin().publik().build();

        assertThat(cache.groups()).hasSize(4);
        assertThat(cache.userPrivates("PETER")).containsExactly(groupB);
    }

    @Test
    void publics_noGroups() {
        assertThat(cache.publics()).isEmpty();
    }

    @Test
    void publics_noPublic() {
        Group group = GroupBuilder.get(cache, 1).group().testadmin().privat().build();

        assertThat(cache.groups()).hasSize(1);
        assertThat(cache.publics()).isEmpty();
    }

    @Test
    void publics_onePublic() {
        Group group = GroupBuilder.get(cache, 1).group().testadmin().publik().build();

        assertThat(cache.groups()).hasSize(1);
        assertThat(cache.publics()).containsExactly(group);
    }

    @Test
    void publics_public_multiple() {
        Group groupA = GroupBuilder.get(cache, 1).group().testadmin().lecture().build();
        Group groupB = GroupBuilder.get(cache, 2).group().testadmin().privat().build();
        Group groupC = GroupBuilder.get(cache, 3).group().testadmin().privat().build();
        Group groupD = GroupBuilder.get(cache, 4).group().testadmin().publik().build();

        assertThat(cache.groups()).hasSize(4);
        assertThat(cache.publics()).containsExactly(groupD);
    }

    @Test
    void privates_noGroups() {
        assertThat(cache.privates()).isEmpty();
    }

    @Test
    void privates_noPrivate() {
        Group group = GroupBuilder.get(cache, 1).group().testadmin().publik().build();

        assertThat(cache.groups()).hasSize(1);
        assertThat(cache.privates()).isEmpty();
    }

    @Test
    void privates_onePrivate() {
        Group group = GroupBuilder.get(cache, 1).group().testadmin().privat().build();

        assertThat(cache.groups()).hasSize(1);
        assertThat(cache.privates()).containsExactly(group);
    }

    @Test
    void privates_private_multiple() {
        Group groupA = GroupBuilder.get(cache, 1).group().testadmin().lecture().build();
        Group groupB = GroupBuilder.get(cache, 2).group().testadmin().privat().build();
        Group groupC = GroupBuilder.get(cache, 3).group().testadmin().privat().build();
        Group groupD = GroupBuilder.get(cache, 4).group().testadmin().publik().build();

        assertThat(cache.groups()).hasSize(4);
        assertThat(cache.privates()).containsExactly(groupB, groupC);
    }

    @Test
    void lectures_noGroups() {
        assertThat(cache.lectures()).isEmpty();
    }

    @Test
    void lectures_noLecture() {
        Group group = GroupBuilder.get(cache, 1).group().testadmin().privat().build();

        assertThat(cache.groups()).hasSize(1);
        assertThat(cache.lectures()).isEmpty();
    }

    @Test
    void lectures_oneLecture() {
        Group group = GroupBuilder.get(cache, 1).group().testadmin().lecture().build();

        assertThat(cache.groups()).hasSize(1);
        assertThat(cache.lectures()).containsExactly(group);
    }

    @Test
    void lectures_lecture_multiple() {
        Group groupA = GroupBuilder.get(cache, 1).group().testadmin().lecture().build();
        Group groupB = GroupBuilder.get(cache, 2).group().testadmin().privat().build();
        Group groupC = GroupBuilder.get(cache, 3).group().testadmin().lecture().build();
        Group groupD = GroupBuilder.get(cache, 4).group().testadmin().publik().build();

        assertThat(cache.groups()).hasSize(4);
        assertThat(cache.lectures()).containsExactly(groupA, groupC);
    }

    //Indirekt: void usersPut() {}

    @Test
    void usersRemove() {
        Group group = GroupBuilder.get(cache, 1).group().testadmin().lecture().limit(2).add("PETER").kick("PETER").build();

        assertThat(cache.groups()).hasSize(1);
        assertThat(cache.userLectures("PETER")).isEmpty();
    }

    //Indirekt: void groupsPut() {}

    @Test
    void groupsRemove() {
        Group group = GroupBuilder.get(cache, 1).group().testadmin().lecture().destroy().build();

        assertThat(cache.groups()).hasSize(0);
    }


    //Indirekt: void linksPut() {}

    @Test
    void linksRemove() {
        Group group = GroupBuilder.get(cache, 1).group().testadmin().lecture().link(1).build();

        assertThat(cache.group(String.valueOf(uuid(1)))).isEqualTo(group);
    }

    //Indirekt: void typesPut() {}

    //Indirekt: void typesRemove() {}
}
