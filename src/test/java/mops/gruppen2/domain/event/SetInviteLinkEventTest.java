package mops.gruppen2.domain.event;

import mops.gruppen2.GroupBuilder;
import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.domain.model.group.wrapper.Link;
import mops.gruppen2.domain.service.EventStoreService;
import mops.gruppen2.infrastructure.GroupCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static mops.gruppen2.TestHelper.uuid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SetInviteLinkEventTest {

    GroupCache cache;

    @BeforeEach
    void setUp() {
        cache = new GroupCache(mock(EventStoreService.class));
    }

    @Test
    void apply() {
        Group group = GroupBuilder.get(cache, 1).group().testadmin().build();
        Event link = new SetInviteLinkEvent(uuid(1), "TEST", new Link(uuid(2).toString()));
        link.init(4);

        link.apply(group);
        assertThat(group.getLink()).isEqualTo(uuid(2).toString());
    }

    @Test
    void apply_cache() {
        Group group = GroupBuilder.get(cache, 1).group().testadmin().build();
        Event link = new SetInviteLinkEvent(uuid(1), "TEST", new Link(uuid(2).toString()));
        link.init(4);

        assertThat(cache.group(group.getLink())).isEqualTo(group);
        link.apply(group, cache);
        assertThat(cache.group(uuid(2).toString())).isEqualTo(group);
    }
}
