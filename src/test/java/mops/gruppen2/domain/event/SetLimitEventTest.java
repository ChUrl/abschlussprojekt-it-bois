package mops.gruppen2.domain.event;

import mops.gruppen2.GroupBuilder;
import mops.gruppen2.domain.exception.BadArgumentException;
import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.domain.model.group.wrapper.Limit;
import mops.gruppen2.domain.service.EventStoreService;
import mops.gruppen2.infrastructure.GroupCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static mops.gruppen2.TestHelper.uuid;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class SetLimitEventTest {

    GroupCache cache;

    @BeforeEach
    void setUp() {
        cache = new GroupCache(mock(EventStoreService.class));
    }

    @Test
    void apply_tooSmall() {
        Group group = GroupBuilder.get(cache, 1).group().testadmin().limit(2).add("PETER").build();
        Event limit = new SetLimitEvent(uuid(1), "TEST", new Limit(1));
        limit.init(6);

        assertThatThrownBy(() -> limit.apply(group))
                .isInstanceOf(BadArgumentException.class);
    }
}
