package mops.gruppen2.domain.event;

import mops.gruppen2.GroupBuilder;
import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.domain.model.group.Type;
import mops.gruppen2.domain.service.EventStoreService;
import mops.gruppen2.infrastructure.GroupCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static mops.gruppen2.TestHelper.uuid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SetTypeEventTest {

    GroupCache cache;

    @BeforeEach
    void setUp() {
        cache = new GroupCache(mock(EventStoreService.class));
    }

    @Test
    void apply_cache() {
        Group group = GroupBuilder.get(cache, 1).group().testadmin().privat().build();
        Event type = new SetTypeEvent(uuid(1), "TEST", Type.LECTURE);
        type.init(5);

        assertThat(cache.privates()).hasSize(1);
        assertThat(cache.lectures()).isEmpty();
        type.apply(group, cache);
        assertThat(cache.lectures()).hasSize(1);
        assertThat(cache.privates()).isEmpty();
    }
}
