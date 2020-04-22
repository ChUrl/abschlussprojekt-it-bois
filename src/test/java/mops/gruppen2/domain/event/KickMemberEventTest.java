package mops.gruppen2.domain.event;

import mops.gruppen2.GroupBuilder;
import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.domain.service.EventStoreService;
import mops.gruppen2.infrastructure.GroupCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static mops.gruppen2.TestHelper.uuid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class KickMemberEventTest {

    GroupCache cache;

    @BeforeEach
    void setUp() {
        cache = new GroupCache(mock(EventStoreService.class));
    }

    @Test
    void apply() {
        Group group = GroupBuilder.get(cache, 1).group().testadmin().build();
        Event kick = new KickMemberEvent(uuid(1), "TEST", "TEST");
        kick.init(4);

        assertThat(group.size()).isOne();
        kick.apply(group);
        assertThat(group.size()).isZero();
    }

    @Test
    void apply_cache() {
        Group group = GroupBuilder.get(cache, 1).group().testadmin().build();
        Event kick = new KickMemberEvent(uuid(1), "TEST", "TEST");
        kick.init(4);

        assertThat(cache.userGroups("TEST")).hasSize(1);
        kick.apply(group, cache);
        assertThat(cache.userGroups("TEST")).hasSize(0);
    }
}
