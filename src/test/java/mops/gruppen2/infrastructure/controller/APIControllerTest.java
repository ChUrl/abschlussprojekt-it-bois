package mops.gruppen2.infrastructure.controller;

import mops.gruppen2.domain.service.EventStoreService;
import mops.gruppen2.infrastructure.GroupCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class APIControllerTest {

    private EventStoreService store;
    private GroupCache cache;
    private APIController controller;

    @BeforeEach
    void setUp() {
        store = mock(EventStoreService.class);
        cache = new GroupCache(store);
        controller = new APIController(cache, store);
    }

    @WithMockUser("ROLE_api_user")
    @Test
    void getApiUpdate_noEvents() {
        when(store.findMaxEventId()).thenReturn(0L);

        assertThat(controller.getApiUpdate(0).getVersion()).isZero();
        assertThat(controller.getApiUpdate(0).getGroups()).isEmpty();
    }

    @Disabled
    @WithMockUser("ROLE_api_user")
    @Test
    void getApiUpdate_noUpdate() {
    }

    @Disabled
    @WithMockUser("ROLE_api_user")
    @Test
    void getApiUserGroups() {
    }

    @Disabled
    @WithMockUser("ROLE_api_user")
    @Test
    void getApiGroup() {
    }
}
