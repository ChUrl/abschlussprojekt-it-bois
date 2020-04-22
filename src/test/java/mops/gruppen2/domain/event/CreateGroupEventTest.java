package mops.gruppen2.domain.event;

import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.domain.service.EventStoreService;
import mops.gruppen2.infrastructure.GroupCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static mops.gruppen2.TestHelper.uuid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CreateGroupEventTest {

    GroupCache cache;

    @BeforeEach
    void setUp() {
        cache = new GroupCache(mock(EventStoreService.class));
    }

    @Test
    void apply() {
        Group group = Group.EMPTY();
        Event add = new CreateGroupEvent(uuid(1), "TEST", LocalDateTime.now());
        add.init(1);

        assertThat(group.exists()).isFalse();
        add.apply(group);
        assertThat(group.exists()).isTrue();
    }

    @Test
    void apply_cache() {
        Group group = Group.EMPTY();
        Event add = new CreateGroupEvent(uuid(1), "TEST", LocalDateTime.now());
        add.init(1);

        add.apply(group, cache);
        assertThat(group.exists()).isTrue();
        assertThat(cache.groups()).hasSize(1);
    }
}
