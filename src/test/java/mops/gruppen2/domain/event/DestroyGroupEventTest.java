package mops.gruppen2.domain.event;

import mops.gruppen2.GroupBuilder;
import mops.gruppen2.domain.exception.NoAccessException;
import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.domain.service.EventStoreService;
import mops.gruppen2.infrastructure.GroupCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static mops.gruppen2.TestHelper.uuid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class DestroyGroupEventTest {

    GroupCache cache;

    @BeforeEach
    void setUp() {
        cache = new GroupCache(mock(EventStoreService.class));
    }

    @Test
    void apply() {
        Group group = GroupBuilder.get(cache, 1).group().testadmin().build();
        Event destroy = new DestroyGroupEvent(uuid(1), "TEST");
        destroy.init(4);

        assertThat(group.exists()).isTrue();
        destroy.apply(group);
        assertThat(group.exists()).isFalse();
    }

    @Test
    void apply_noadmin() {
        Group group = GroupBuilder.get(cache, 1).group().testadmin().limit(3).add("PETER").build();
        Event destroy = new DestroyGroupEvent(uuid(1), "PETER");
        destroy.init(6);

        assertThatThrownBy(() -> destroy.apply(group))
                .isInstanceOf(NoAccessException.class);
    }

    @Test
    void apply_noadmin_empty() {
        Group group = GroupBuilder.get(cache, 1).group().build();
        Event destroy = new DestroyGroupEvent(uuid(1), "PETER");
        destroy.init(2);

        destroy.apply(group);
    }

    @Test
    void apply_cache_private() {
        Group group = GroupBuilder.get(cache, 1).group().testadmin().privat().build();
        Event destroy = new DestroyGroupEvent(uuid(1), "TEST");
        destroy.init(5);

        assertThat(cache.groups()).hasSize(1);
        assertThat(cache.userGroups("TEST")).hasSize(1);
        assertThat(cache.privates()).hasSize(1);
        destroy.apply(group, cache);
        assertThat(cache.groups()).isEmpty();
        assertThat(cache.userGroups("TEST")).isEmpty();
        assertThat(cache.privates()).isEmpty();
    }

    @Test
    void apply_cache_public() {
        Group group = GroupBuilder.get(cache, 1).group().testadmin().publik().build();
        Event destroy = new DestroyGroupEvent(uuid(1), "TEST");
        destroy.init(5);

        assertThat(cache.publics()).hasSize(1);
        destroy.apply(group, cache);
        assertThat(cache.publics()).isEmpty();
    }

    @Test
    void apply_cache_lecture() {
        Group group = GroupBuilder.get(cache, 1).group().testadmin().lecture().build();
        Event destroy = new DestroyGroupEvent(uuid(1), "TEST");
        destroy.init(5);

        assertThat(cache.lectures()).hasSize(1);
        destroy.apply(group, cache);
        assertThat(cache.lectures()).isEmpty();
    }

    @Test
    void apply_cache_multipleUsers() {
        Group group = GroupBuilder.get(cache, 1).group().testadmin().privat().limit(5).add("A").add("B").add("C").add("D").build();
        Event destroy = new DestroyGroupEvent(uuid(1), "TEST");
        destroy.init(10);

        assertThat(cache.userGroups("TEST")).hasSize(1);
        assertThat(cache.userGroups("A")).hasSize(1);
        assertThat(cache.userGroups("B")).hasSize(1);
        assertThat(cache.userGroups("C")).hasSize(1);
        assertThat(cache.userGroups("D")).hasSize(1);
        destroy.apply(group, cache);
        assertThat(cache.userGroups("TEST")).hasSize(0);
        assertThat(cache.userGroups("A")).hasSize(0);
        assertThat(cache.userGroups("B")).hasSize(0);
        assertThat(cache.userGroups("C")).hasSize(0);
        assertThat(cache.userGroups("D")).hasSize(0);
    }
}
