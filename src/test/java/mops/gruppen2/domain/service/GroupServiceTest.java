package mops.gruppen2.domain.service;

import mops.gruppen2.GroupBuilder;
import mops.gruppen2.domain.exception.BadArgumentException;
import mops.gruppen2.domain.exception.GroupFullException;
import mops.gruppen2.domain.exception.LastAdminException;
import mops.gruppen2.domain.exception.NoAccessException;
import mops.gruppen2.domain.exception.UserExistsException;
import mops.gruppen2.domain.exception.UserNotFoundException;
import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.domain.model.group.Type;
import mops.gruppen2.domain.model.group.User;
import mops.gruppen2.domain.model.group.wrapper.Description;
import mops.gruppen2.domain.model.group.wrapper.Limit;
import mops.gruppen2.domain.model.group.wrapper.Link;
import mops.gruppen2.domain.model.group.wrapper.Parent;
import mops.gruppen2.domain.model.group.wrapper.Title;
import mops.gruppen2.infrastructure.GroupCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static mops.gruppen2.TestHelper.uuid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class GroupServiceTest {

    private GroupService groupService;

    @BeforeEach
    void setUp() {
        groupService = new GroupService(mock(GroupCache.class), mock(EventStoreService.class));
    }

    @Test
    void createGroup() {
        Group group = groupService.createGroup("TEST");

        assertThat(group.getId()).isNotNull();
        assertThat(group.creator()).isEqualTo("TEST");
    }

    @Test
    void initGroupMembers() {
        Group group = groupService.createGroup("TEST");
        groupService.initGroupMembers(group, "TEST", "TEST", new User("TEST"), new Limit(1));

        assertThat(group.getMembers()).containsExactly(new User("TEST"));
        assertThat(group.getLimit()).isEqualTo(1);
        assertThat(group.isAdmin("TEST")).isTrue();
    }

    @Test
    void initGroupMeta() {
        Group group = groupService.createGroup("TEST");
        groupService.initGroupMembers(group, "TEST", "TEST", new User("TEST"), new Limit(1));
        groupService.initGroupMeta(group, "TEST", Type.PUBLIC, Parent.EMPTY());

        assertThat(group.isPublic()).isTrue();
        assertThat(group.hasParent()).isFalse();
    }

    @Test
    void initGroupText() {
        Group group = groupService.createGroup("TEST");
        groupService.initGroupMembers(group, "TEST", "TEST", new User("TEST"), new Limit(1));
        groupService.initGroupMeta(group, "TEST", Type.PUBLIC, Parent.EMPTY());
        groupService.initGroupText(group, "TEST", new Title("TITLE"), new Description("DESCR"));

        assertThat(group.getTitle()).isEqualTo("TITLE");
        assertThat(group.getDescription()).isEqualTo("DESCR");
    }

    @Test
    void addUsersToGroup() {
        Group group = GroupBuilder.get(mock(GroupCache.class), 1).group().testadmin().build();

        groupService.addUsersToGroup(group, "TEST", Arrays.asList(
                new User("A"),
                new User("B"),
                new User("C"),
                new User("C")));

        assertThat(group.getLimit()).isEqualTo(4);
        assertThat(group.size()).isEqualTo(4);
        assertThat(group.getRegulars()).hasSize(3);
        assertThat(group.getAdmins()).hasSize(1);
    }

    @Test
    void toggleMemberRole_lastAdmin_lastUser() {
        Group group = GroupBuilder.get(mock(GroupCache.class), 1).group().testadmin().build();

        groupService.toggleMemberRole(group, "TEST", "TEST");
    }

    @Test
    void toggleMemberRole_lastAdmin() {
        Group group = GroupBuilder.get(mock(GroupCache.class), 1).group().testadmin().limit(2).add("PETER").build();

        assertThatThrownBy(() -> groupService.toggleMemberRole(group, "TEST", "TEST"))
                .isInstanceOf(LastAdminException.class);
    }

    @Test
    void toggleMemberRole_noMember() {
        Group group = GroupBuilder.get(mock(GroupCache.class), 1).group().testadmin().build();

        assertThatThrownBy(() -> groupService.toggleMemberRole(group, "TEST", "PETER"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void addMember_newMember() {
        Group group = GroupBuilder.get(mock(GroupCache.class), 1).group().testadmin().limit(2).build();

        groupService.addMember(group, "Test", "PETER", new User("PETER"));

        assertThat(group.size()).isEqualTo(2);
        assertThat(group.getAdmins()).hasSize(1);
        assertThat(group.getRegulars()).hasSize(1);
    }

    @Test
    void addMember_newMember_groupFull() {
        Group group = GroupBuilder.get(mock(GroupCache.class), 1).group().testadmin().build();

        assertThatThrownBy(() -> groupService.addMember(group, "Test", "PETER", new User("PETER")))
                .isInstanceOf(GroupFullException.class);
    }

    @Test
    void addMember_newMember_userExists() {
        Group group = GroupBuilder.get(mock(GroupCache.class), 1).group().testadmin().limit(3).add("PETER").build();

        assertThatThrownBy(() -> groupService.addMember(group, "Test", "PETER", new User("PETER")))
                .isInstanceOf(UserExistsException.class);
    }

    @Test
    void kickMember_noMember() {
        Group group = GroupBuilder.get(mock(GroupCache.class), 1).group().testadmin().build();

        assertThatThrownBy(() -> groupService.kickMember(group, "TEST", "PETER"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void kickMember_lastMember() {
        Group group = GroupBuilder.get(mock(GroupCache.class), 1).group().testadmin().build();

        groupService.kickMember(group, "TEST", "TEST");

        assertThat(group.exists()).isFalse();
    }

    @Test
    void kickMember_lastAdmin() {
        Group group = GroupBuilder.get(mock(GroupCache.class), 1).group().testadmin().limit(2).add("PETER").build();

        assertThatThrownBy(() -> groupService.kickMember(group, "TEST", "TEST"))
                .isInstanceOf(LastAdminException.class);
    }

    @Test
    void deleteGroup_noGroup() {
        groupService.deleteGroup(Group.EMPTY(), "TEST");
    }

    @Test
    void deleteGroup() {
        Group group = GroupBuilder.get(mock(GroupCache.class), 1).group().testadmin().limit(2).add("PETER").build();

        groupService.deleteGroup(group, "TEST");

        assertThat(group.exists()).isFalse();
    }

    @Test
    void deleteGroup_noAdmin() {
        Group group = GroupBuilder.get(mock(GroupCache.class), 1).group().testadmin().limit(2).add("PETER").build();

        assertThatThrownBy(() -> groupService.deleteGroup(group, "PETER"))
                .isInstanceOf(NoAccessException.class);
        assertThat(group.exists()).isTrue();
    }

    @Test
    void setTitle() {
        Group group = GroupBuilder.get(mock(GroupCache.class), 1).group().testadmin().build();

        groupService.setTitle(group, "TEST", new Title("TITLE"));

        assertThat(group.getTitle()).isEqualTo("TITLE");
    }

    @Test
    void setDescription() {
        Group group = GroupBuilder.get(mock(GroupCache.class), 1).group().testadmin().build();

        groupService.setDescription(group, "TEST", new Description("DESCR"));

        assertThat(group.getDescription()).isEqualTo("DESCR");
    }

    @Test
    void setLimit_tooLow() {
        Group group = GroupBuilder.get(mock(GroupCache.class), 1).group().testadmin().limit(2).add("PETER").build();

        assertThatThrownBy(() -> groupService.setLimit(group, "TEST", new Limit(1)))
                .isInstanceOf(BadArgumentException.class);

        assertThat(group.getLimit()).isEqualTo(2);
    }

    @Test
    void setLimit() {
        Group group = GroupBuilder.get(mock(GroupCache.class), 1).group().testadmin().build();

        groupService.setLimit(group, "TEST", new Limit(8));

        assertThat(group.getLimit()).isEqualTo(8);
    }

    @Test
    void setParent_sameGroup() {
        Group group = GroupBuilder.get(mock(GroupCache.class), 1).group().testadmin().build();

        assertThatThrownBy(() -> groupService.setParent(group, "TEST", new Parent(uuid(1).toString())))
                .isInstanceOf(BadArgumentException.class);

        assertThat(group.getParent()).isEqualTo(uuid(0));
        assertThat(group.hasParent()).isFalse();
    }

    @Test
    void setParent() {
        Group group = GroupBuilder.get(mock(GroupCache.class), 1).group().testadmin().build();

        groupService.setParent(group, "TEST", new Parent(uuid(2).toString()));

        assertThat(group.getParent()).isEqualTo(uuid(2));
        assertThat(group.hasParent()).isTrue();
    }

    @Test
    void setLink_sameAsGroupId() {
        Group group = GroupBuilder.get(mock(GroupCache.class), 1).group().testadmin().build();

        assertThatThrownBy(() -> groupService.setLink(group, "TEST", new Link(uuid(1).toString())))
                .isInstanceOf(BadArgumentException.class);

        assertThat(group.getLink()).isNotEqualTo(uuid(1).toString());
    }

    @Test
    void setLink() {
        Group group = GroupBuilder.get(mock(GroupCache.class), 1).group().testadmin().build();

        groupService.setLink(group, "TEST", new Link(uuid(2).toString()));

        assertThat(group.getLink()).isEqualTo(uuid(2).toString());
    }
}
