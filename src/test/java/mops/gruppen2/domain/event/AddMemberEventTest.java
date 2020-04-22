package mops.gruppen2.domain.event;

import mops.gruppen2.GroupBuilder;
import mops.gruppen2.TestHelper;
import mops.gruppen2.domain.exception.GroupFullException;
import mops.gruppen2.domain.exception.IdMismatchException;
import mops.gruppen2.domain.exception.UserExistsException;
import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.domain.model.group.User;
import mops.gruppen2.domain.service.EventStoreService;
import mops.gruppen2.infrastructure.GroupCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class AddMemberEventTest {

    GroupCache cache;

    @BeforeEach
    void setUp() {
        cache = new GroupCache(mock(EventStoreService.class));
    }

    @Test
    void userMismatch() {
        assertThatThrownBy(() -> new AddMemberEvent(TestHelper.uuid(1), "TEST", "TEST", new User("PETER")))
                .isInstanceOf(IdMismatchException.class);
    }

    @Test
    void apply() {
        Group group = GroupBuilder.get(cache, 1).group().build();
        Event add = new AddMemberEvent(TestHelper.uuid(1), "TEST", "TEST", new User("TEST"));
        add.init(2);

        add.apply(group);

        assertThat(group.getMembers()).containsExactly(new User("TEST"));
    }

    @Test
    void apply_cache() {
        Group group = GroupBuilder.get(cache, 1).group().build();
        Event add = new AddMemberEvent(TestHelper.uuid(1), "TEST", "TEST", new User("TEST"));
        add.init(2);

        add.apply(group, cache);

        assertThat(group.getMembers()).containsExactly(new User("TEST"));
        assertThat(cache.userGroups("TEST")).containsExactly(group);
    }

    @Test
    void apply_userExists() {
        Group group = GroupBuilder.get(cache, 1).group().testadmin().limit(2).build();
        Event add = new AddMemberEvent(TestHelper.uuid(1), "TEST", "TEST", new User("TEST"));
        add.init(5);

        assertThatThrownBy(() -> add.apply(group, cache))
                .isInstanceOf(UserExistsException.class);
    }

    @Test
    void apply_groupFull() {
        Group group = GroupBuilder.get(cache, 1).group().testadmin().build();
        Event add = new AddMemberEvent(TestHelper.uuid(1), "TEST", "PETER", new User("PETER"));
        add.init(4);

        assertThatThrownBy(() -> add.apply(group, cache))
                .isInstanceOf(GroupFullException.class);
    }
}
