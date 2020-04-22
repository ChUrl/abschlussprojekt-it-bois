package mops.gruppen2.domain.event;

import mops.gruppen2.GroupBuilder;
import mops.gruppen2.domain.exception.BadArgumentException;
import mops.gruppen2.domain.exception.IdMismatchException;
import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.domain.model.group.User;
import mops.gruppen2.infrastructure.GroupCache;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static mops.gruppen2.TestHelper.uuid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class EventTest {

    @Test
    void apply_smallVersion() {
        Group group = GroupBuilder.get(Mockito.mock(GroupCache.class), 1).group().build();
        Event add = new AddMemberEvent(uuid(1), "TEST", "TEST", new User("TEST"));
        add.init(1);

        assertThatThrownBy(() -> add.apply(group))
                .isInstanceOf(IdMismatchException.class);
    }

    @Test
    void apply_bigVersion() {
        Group group = GroupBuilder.get(Mockito.mock(GroupCache.class), 1).group().build();
        Event add = new AddMemberEvent(uuid(1), "TEST", "TEST", new User("TEST"));
        add.init(3);

        assertThatThrownBy(() -> add.apply(group))
                .isInstanceOf(IdMismatchException.class);
    }

    @Test
    void apply_notInitialized() {
        Group group = GroupBuilder.get(Mockito.mock(GroupCache.class), 1).group().build();
        Event add = new AddMemberEvent(uuid(1), "TEST", "TEST", new User("TEST"));

        assertThatThrownBy(() -> add.apply(group))
                .isInstanceOf(BadArgumentException.class);
    }

    @Test
    void apply_wrongGroup() {
        Group group = GroupBuilder.get(Mockito.mock(GroupCache.class), 1).group().build();
        Event add = new AddMemberEvent(uuid(2), "TEST", "TEST", new User("TEST"));
        add.init(2);

        assertThatThrownBy(() -> add.apply(group))
                .isInstanceOf(IdMismatchException.class);
    }

    @Test
    void apply_updateVersion() {
        Group group = GroupBuilder.get(Mockito.mock(GroupCache.class), 1).group().build();
        Event add = new AddMemberEvent(uuid(1), "TEST", "TEST", new User("TEST"));
        add.init(2);

        assertThat(group.getVersion()).isEqualTo(1);
        add.apply(group);
        assertThat(group.getVersion()).isEqualTo(2);
    }

    @Test
    void init_alreadyInitialized() {
        Event add = new AddMemberEvent(uuid(1), "TEST", "TEST", new User("TEST"));
        add.init(2);

        assertThatThrownBy(() -> add.init(3))
                .isInstanceOf(BadArgumentException.class);
    }
}
