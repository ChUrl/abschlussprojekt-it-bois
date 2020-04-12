package mops.gruppen2.web;

import mops.gruppen2.Gruppen2Application;
import mops.gruppen2.domain.exception.GroupNotFoundException;
import mops.gruppen2.domain.service.EventStoreService;
import mops.gruppen2.persistance.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static mops.gruppen2.TestBuilder.addUserEvent;
import static mops.gruppen2.TestBuilder.createPrivateGroupEvent;
import static mops.gruppen2.TestBuilder.createPublicGroupEvent;
import static mops.gruppen2.TestBuilder.deleteGroupEvent;
import static mops.gruppen2.TestBuilder.deleteUserEvent;
import static mops.gruppen2.TestBuilder.updateGroupTitleEvent;
import static mops.gruppen2.TestBuilder.updateUserLimitMaxEvent;
import static mops.gruppen2.TestBuilder.uuidMock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Gruppen2Application.class)
@Transactional
@Rollback
class APIControllerTest {

    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private APIController apiController;
    @Autowired
    private EventStoreService eventStoreService;
    @Autowired
    private JdbcTemplate template;

    @SuppressWarnings("SyntaxError")
    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        //noinspection SqlResolve
        template.execute("ALTER TABLE event ALTER COLUMN event_id RESTART WITH 1");
    }

    @Test
    @WithMockUser(username = "api_user", roles = "api_user")
    void updateGroup_noGroup() {
        assertThat(apiController.getApiUpdate(0L).getGroupList()).hasSize(0);
        assertThat(apiController.getApiUpdate(4L).getGroupList()).hasSize(0);
        assertThat(apiController.getApiUpdate(10L).getGroupList()).hasSize(0);
        assertThat(apiController.getApiUpdate(0L).getStatus()).isEqualTo(0);
    }

    @Test
    @WithMockUser(username = "api_user", roles = "api_user")
    void updateGroup_singleGroup() {
        eventStoreService.saveAll(createPublicGroupEvent(uuidMock(0)),
                                  updateUserLimitMaxEvent(uuidMock(0)),
                                  addUserEvent(uuidMock(0)),
                                  addUserEvent(uuidMock(0)),
                                  addUserEvent(uuidMock(0)),
                                  addUserEvent(uuidMock(0)));

        assertThat(apiController.getApiUpdate(0L).getGroupList()).hasSize(1);
        assertThat(apiController.getApiUpdate(4L).getGroupList()).hasSize(1);
        assertThat(apiController.getApiUpdate(10L).getGroupList()).hasSize(0);
        assertThat(apiController.getApiUpdate(0L).getStatus()).isEqualTo(6);
    }


    @Test
    @WithMockUser(username = "api_user", roles = "api_user")
    void updateGroup_multipleGroups() {
        eventStoreService.saveAll(createPublicGroupEvent(uuidMock(0)),
                                  updateUserLimitMaxEvent(uuidMock(0)),
                                  addUserEvent(uuidMock(0)),
                                  addUserEvent(uuidMock(0)),
                                  createPrivateGroupEvent(uuidMock(1)),
                                  updateUserLimitMaxEvent(uuidMock(1)),
                                  addUserEvent(uuidMock(1)),
                                  addUserEvent(uuidMock(1)),
                                  addUserEvent(uuidMock(1)));

        assertThat(apiController.getApiUpdate(0L).getGroupList()).hasSize(2);
        assertThat(apiController.getApiUpdate(4L).getGroupList()).hasSize(1);
        assertThat(apiController.getApiUpdate(6L).getGroupList()).hasSize(1);
        assertThat(apiController.getApiUpdate(7L).getGroupList()).hasSize(1);
        assertThat(apiController.getApiUpdate(0L).getStatus()).isEqualTo(9);
    }

    @Test
    @WithMockUser(username = "api_user", roles = "api_user")
    void getGroupsOfUser_noGroup() {
        assertThat(apiController.getApiUserGroups("A")).isEmpty();
    }

    @Test
    @WithMockUser(username = "api_user", roles = "api_user")
    void getGroupsOfUser_singleGroup() {
        eventStoreService.saveAll(createPrivateGroupEvent(uuidMock(0)),
                                  createPrivateGroupEvent(uuidMock(1)),
                                  createPrivateGroupEvent(uuidMock(2)),
                                  addUserEvent(uuidMock(0), "A"));

        assertThat(apiController.getApiUserGroups("A")).hasSize(1);
    }

    @Test
    @WithMockUser(username = "api_user", roles = "api_user")
    void getGroupsOfUser_singleGroupDeletedUser() {
        eventStoreService.saveAll(createPrivateGroupEvent(uuidMock(0)),
                                  addUserEvent(uuidMock(0), "A"),
                                  deleteUserEvent(uuidMock(0), "A"));

        assertThat(apiController.getApiUserGroups("A")).isEmpty();
    }

    @Disabled
    @Test
    @WithMockUser(username = "api_user", roles = "api_user")
    void getGroupsOfUser_singleDeletedGroup() {
        eventStoreService.saveAll(createPrivateGroupEvent(uuidMock(0)),
                                  addUserEvent(uuidMock(0), "A"),
                                  deleteGroupEvent(uuidMock(0)));

        assertThat(apiController.getApiUserGroups("A")).isEmpty();
    }

    @Test
    @WithMockUser(username = "api_user", roles = "api_user")
    void getGroupsOfUser_multipleGroups() {
        eventStoreService.saveAll(createPrivateGroupEvent(uuidMock(0)),
                                  createPrivateGroupEvent(uuidMock(1)),
                                  createPrivateGroupEvent(uuidMock(2)),
                                  addUserEvent(uuidMock(0), "A"),
                                  addUserEvent(uuidMock(0), "B"),
                                  addUserEvent(uuidMock(1), "A"),
                                  addUserEvent(uuidMock(2), "A"),
                                  addUserEvent(uuidMock(2), "B"));

        assertThat(apiController.getApiUserGroups("A")).hasSize(3);
        assertThat(apiController.getApiUserGroups("B")).hasSize(2);
    }

    @Test
    @WithMockUser(username = "api_user", roles = "api_user")
    void getGroupFromId_noGroup() {
        assertThrows(GroupNotFoundException.class, () -> apiController.getApiGroup(uuidMock(0).toString()));
    }

    @Test
    @WithMockUser(username = "api_user", roles = "api_user")
    void getGroupFromId_singleGroup() {
        eventStoreService.saveAll(createPrivateGroupEvent(uuidMock(0)));

        assertThat(apiController.getApiGroup(uuidMock(0).toString()).getId()).isEqualTo(uuidMock(0));
    }

    @Test
    @WithMockUser(username = "api_user", roles = "api_user")
    void getGroupFromId_deletedGroup() {
        eventStoreService.saveAll(createPrivateGroupEvent(uuidMock(0)),
                                  updateGroupTitleEvent(uuidMock(0)),
                                  deleteGroupEvent(uuidMock(0)));

        assertThat(apiController.getApiGroup(uuidMock(0).toString()).getTitle()).isEqualTo(null);
    }
}
