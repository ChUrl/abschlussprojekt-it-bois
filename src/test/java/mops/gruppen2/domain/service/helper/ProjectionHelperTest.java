package mops.gruppen2.domain.service.helper;

import mops.gruppen2.domain.event.AddMemberEvent;
import mops.gruppen2.domain.event.CreateGroupEvent;
import mops.gruppen2.domain.event.Event;
import mops.gruppen2.domain.event.SetLimitEvent;
import mops.gruppen2.domain.event.UpdateRoleEvent;
import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.domain.model.group.Role;
import mops.gruppen2.domain.model.group.User;
import mops.gruppen2.domain.model.group.wrapper.Limit;
import mops.gruppen2.infrastructure.GroupCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static mops.gruppen2.TestHelper.initEvents;
import static mops.gruppen2.TestHelper.uuid;
import static mops.gruppen2.domain.service.helper.ProjectionHelper.project;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ProjectionHelperTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void project_nocache_emptyList() {
        assertThat(project(Collections.emptyList())).isEmpty();
    }

    @Test
    void project_nocache_oneCreate() {
        List<Event> events = Arrays.asList(
                new CreateGroupEvent(uuid(1), "TEST", LocalDateTime.now()));

        initEvents(events);

        assertThat(project(events)).hasSize(1);
    }

    @Test
    void project_nocache_multipleCreate() {
        List<Event> events = Arrays.asList(
                new CreateGroupEvent(uuid(1), "TEST", LocalDateTime.now()),
                new CreateGroupEvent(uuid(2), "TEST", LocalDateTime.now()),
                new CreateGroupEvent(uuid(3), "TEST", LocalDateTime.now()),
                new CreateGroupEvent(uuid(4), "TEST", LocalDateTime.now()),
                new CreateGroupEvent(uuid(5), "TEST", LocalDateTime.now()));

        initEvents(events);

        assertThat(project(events)).hasSize(5);
    }

    @Test
    void project_nocache_oneDetailed() {
        List<Event> events = Arrays.asList(
                new CreateGroupEvent(uuid(1), "TEST", LocalDateTime.now()),
                new AddMemberEvent(uuid(1), "TEST", "TEST", new User("TEST")),
                new UpdateRoleEvent(uuid(1), "TEST", "TEST", Role.ADMIN),
                new SetLimitEvent(uuid(1), "TEST", new Limit(5)));

        initEvents(events);

        List<Group> groups = project(events);

        assertThat(groups).hasSize(1);
        assertThat(groups.get(0).exists()).isTrue();
        assertThat(groups.get(0).getAdmins()).hasSize(1);
        assertThat(groups.get(0).getLimit()).isEqualTo(5);
    }

    @Test
    void project_nocache_multipleDetailed() {
        List<Event> events = Arrays.asList(
                new CreateGroupEvent(uuid(1), "TEST", LocalDateTime.now()),
                new AddMemberEvent(uuid(1), "TEST", "TEST", new User("TEST")),
                new UpdateRoleEvent(uuid(1), "TEST", "TEST", Role.ADMIN),
                new SetLimitEvent(uuid(1), "TEST", new Limit(5)),

                new CreateGroupEvent(uuid(2), "TEST", LocalDateTime.now()),
                new AddMemberEvent(uuid(2), "TEST", "TEST", new User("TEST")),
                new UpdateRoleEvent(uuid(2), "TEST", "TEST", Role.ADMIN),
                new SetLimitEvent(uuid(2), "TEST", new Limit(15)),

                new CreateGroupEvent(uuid(3), "TEST", LocalDateTime.now()),
                new AddMemberEvent(uuid(3), "TEST", "TEST", new User("TEST")),
                new UpdateRoleEvent(uuid(3), "TEST", "TEST", Role.ADMIN),
                new SetLimitEvent(uuid(3), "TEST", new Limit(25)),

                new CreateGroupEvent(uuid(4), "TEST", LocalDateTime.now()),
                new AddMemberEvent(uuid(4), "TEST", "TEST", new User("TEST")),
                new UpdateRoleEvent(uuid(4), "TEST", "TEST", Role.ADMIN),
                new SetLimitEvent(uuid(4), "TEST", new Limit(35)));

        initEvents(events);

        List<Group> groups = project(events);

        assertThat(groups).hasSize(4);
        assertThat(groups.get(0).exists()).isTrue();
        assertThat(groups.get(0).getAdmins()).hasSize(1);
        assertThat(groups.get(0).getLimit()).isEqualTo(5);

        assertThat(groups.get(1).exists()).isTrue();
        assertThat(groups.get(1).getAdmins()).hasSize(1);
        assertThat(groups.get(1).getLimit()).isEqualTo(15);

        assertThat(groups.get(2).exists()).isTrue();
        assertThat(groups.get(2).getAdmins()).hasSize(1);
        assertThat(groups.get(2).getLimit()).isEqualTo(25);

        assertThat(groups.get(3).exists()).isTrue();
        assertThat(groups.get(3).getAdmins()).hasSize(1);
        assertThat(groups.get(3).getLimit()).isEqualTo(35);
    }

    @Test
    void project_cache_noGroups() {
        Map<UUID, Group> groups = new HashMap<>();

        project(groups, Collections.emptyList(), mock(GroupCache.class));

        assertThat(groups).isEmpty();
    }

    @Test
    void project_cache_oneCreate() {
        Map<UUID, Group> groups = new HashMap<>();
        List<Event> events = Arrays.asList(
                new CreateGroupEvent(uuid(1), "TEST", LocalDateTime.now()));

        initEvents(events);
        project(groups, events, mock(GroupCache.class));

        assertThat(groups).hasSize(1);
        assertThat(groups.keySet()).containsExactly(uuid(1));
    }

    @Test
    void project_cache_multipleCreate() {
        Map<UUID, Group> groups = new HashMap<>();
        List<Event> events = Arrays.asList(
                new CreateGroupEvent(uuid(1), "TEST", LocalDateTime.now()),
                new CreateGroupEvent(uuid(2), "TEST", LocalDateTime.now()),
                new CreateGroupEvent(uuid(3), "TEST", LocalDateTime.now()),
                new CreateGroupEvent(uuid(4), "TEST", LocalDateTime.now()));

        initEvents(events);
        project(groups, events, mock(GroupCache.class));

        assertThat(groups).hasSize(4);
        assertThat(groups.keySet()).containsExactly(uuid(1), uuid(2), uuid(3), uuid(4));
    }

    @Test
    void project_cache_multipleDetailed() {
        Map<UUID, Group> groups = new HashMap<>();
        List<Event> events = Arrays.asList(
                new CreateGroupEvent(uuid(1), "TEST", LocalDateTime.now()),
                new AddMemberEvent(uuid(1), "TEST", "TEST", new User("TEST")),
                new UpdateRoleEvent(uuid(1), "TEST", "TEST", Role.ADMIN),
                new SetLimitEvent(uuid(1), "TEST", new Limit(5)),

                new CreateGroupEvent(uuid(2), "TEST", LocalDateTime.now()),
                new AddMemberEvent(uuid(2), "TEST", "TEST", new User("TEST")),
                new UpdateRoleEvent(uuid(2), "TEST", "TEST", Role.ADMIN),
                new SetLimitEvent(uuid(2), "TEST", new Limit(15)),

                new CreateGroupEvent(uuid(3), "TEST", LocalDateTime.now()),
                new AddMemberEvent(uuid(3), "TEST", "TEST", new User("TEST")),
                new UpdateRoleEvent(uuid(3), "TEST", "TEST", Role.ADMIN),
                new SetLimitEvent(uuid(3), "TEST", new Limit(25)),

                new CreateGroupEvent(uuid(4), "TEST", LocalDateTime.now()),
                new AddMemberEvent(uuid(4), "TEST", "TEST", new User("TEST")),
                new UpdateRoleEvent(uuid(4), "TEST", "TEST", Role.ADMIN),
                new SetLimitEvent(uuid(4), "TEST", new Limit(35)));

        initEvents(events);
        project(groups, events, mock(GroupCache.class));

        assertThat(groups).hasSize(4);
        assertThat(groups.get(uuid(1)).exists()).isTrue();
        assertThat(groups.get(uuid(1)).getAdmins()).hasSize(1);
        assertThat(groups.get(uuid(1)).getLimit()).isEqualTo(5);

        assertThat(groups.get(uuid(2)).exists()).isTrue();
        assertThat(groups.get(uuid(2)).getAdmins()).hasSize(1);
        assertThat(groups.get(uuid(2)).getLimit()).isEqualTo(15);

        assertThat(groups.get(uuid(3)).exists()).isTrue();
        assertThat(groups.get(uuid(3)).getAdmins()).hasSize(1);
        assertThat(groups.get(uuid(3)).getLimit()).isEqualTo(25);

        assertThat(groups.get(uuid(4)).exists()).isTrue();
        assertThat(groups.get(uuid(4)).getAdmins()).hasSize(1);
        assertThat(groups.get(uuid(4)).getLimit()).isEqualTo(35);
    }
}
