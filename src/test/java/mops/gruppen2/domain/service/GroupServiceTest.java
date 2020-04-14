package mops.gruppen2.domain.service;

import mops.gruppen2.Gruppen2Application;
import mops.gruppen2.TestBuilder;
import mops.gruppen2.domain.event.Event;
import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.domain.model.group.Type;
import mops.gruppen2.domain.model.group.User;
import mops.gruppen2.persistance.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static mops.gruppen2.TestBuilder.addUserEvent;
import static mops.gruppen2.TestBuilder.completePrivateGroup;
import static mops.gruppen2.TestBuilder.completePrivateGroups;
import static mops.gruppen2.TestBuilder.completePublicGroups;
import static mops.gruppen2.TestBuilder.createLectureEvent;
import static mops.gruppen2.TestBuilder.createPrivateGroupEvent;
import static mops.gruppen2.TestBuilder.createPublicGroupEvent;
import static mops.gruppen2.TestBuilder.deleteGroupEvent;
import static mops.gruppen2.TestBuilder.updateGroupDescriptionEvent;
import static mops.gruppen2.TestBuilder.updateGroupTitleEvent;
import static mops.gruppen2.TestBuilder.uuidMock;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Gruppen2Application.class)
@Transactional
@Rollback
class GroupServiceTest {

    @Autowired
    private EventRepository eventRepository;
    @Autowired
    SearchService searchService;
    private GroupService groupService;
    @Autowired
    private JdbcTemplate template;
    @Autowired
    ProjectionService projectionService;
    @Autowired
    private EventStoreService eventStoreService;
    @Autowired
    private InviteService inviteService;

    @SuppressWarnings("SyntaxError")
    @BeforeEach
    void setUp() {
        groupService = new GroupService(eventStoreService, inviteService);
        eventRepository.deleteAll();
        //noinspection SqlResolve
        template.execute("ALTER TABLE event ALTER COLUMN event_id RESTART WITH 1");
    }

    //TODO: Wofür ist dieser Test?
    //TODO: ProjectionServiceTest
    @Test
    void rightClassForSuccessfulGroup() {
        List<Event> eventList = completePrivateGroup(1);

        List<Group> groups = ProjectionService.projectGroups(eventList);
        assertThat(groups.get(0)).isInstanceOf(Group.class);
    }

    //TODO: ProjectionServiceTest
    @Test
    void projectEventList_SingleGroup() {
        List<Event> eventList = completePrivateGroup(5);

        List<Group> groups = ProjectionService.projectGroups(eventList);

        assertThat(groups).hasSize(1);
        assertThat(groups.get(0).getMemberships()).hasSize(5);
        assertThat(groups.get(0).getType()).isEqualTo(Type.PRIVATE);
    }

    //TODO: ProjectionServiceTest
    @Test
    void projectEventList_MultipleGroups() {
        List<Event> eventList = completePrivateGroups(10, 2);
        eventList.addAll(completePublicGroups(10, 5));

        List<Group> groups = ProjectionService.projectGroups(eventList);

        assertThat(groups).hasSize(20);
        assertThat(groups.stream().map(group -> group.getMemberships().size()).reduce(Integer::sum).get()).isEqualTo(70);
    }

    //TODO: EventStoreServiceTest
    @Test
    void getGroupEvents() {
        eventStoreService.saveAll(createPublicGroupEvent(uuidMock(0)),
                                  createPublicGroupEvent(uuidMock(1)),
                                  createPrivateGroupEvent(uuidMock(2)));

        List<UUID> groupIds = Arrays.asList(uuidMock(0), uuidMock(1));

        assertThat(eventStoreService.findGroupEvents(groupIds)).hasSize(2);
        assertThat(eventStoreService.findGroupEvents(groupIds).get(0).getGroupid()).isEqualTo(uuidMock(0));
        assertThat(eventStoreService.findGroupEvents(groupIds).get(1).getGroupid()).isEqualTo(uuidMock(1));
    }

    //TODO: ProjectionServiceTest
    @Disabled
    @Test
    void getAllGroupWithVisibilityPublicTestCreateAndDeleteSameGroup() {
        Event test1 = createPublicGroupEvent(uuidMock(0));
        Event test2 = deleteGroupEvent(uuidMock(0));

        //TODO: Hier projectEventlist()?
        Group group = TestBuilder.apply(test1, test2);

        assertThat(group.getType()).isEqualTo(null);
        assertThat(projectionService.projectPublicGroups()).isEmpty();
    }

    //TODO: ProjectionServiceTest
    @Disabled
    @Test
    void getAllGroupWithVisibilityPublicTestGroupPublic() {
        eventStoreService.saveAll(createPublicGroupEvent(uuidMock(0)),
                                  deleteGroupEvent(uuidMock(0)),
                                  createPublicGroupEvent());

        assertThat(projectionService.projectPublicGroups().size()).isEqualTo(1);
    }

    //TODO: ProjectionServiceTest
    @Disabled
    @Test
    void getAllGroupWithVisibilityPublicTestAddSomeEvents() {
        eventStoreService.saveAll(createPublicGroupEvent(uuidMock(0)),
                                  deleteGroupEvent(uuidMock(0)),
                                  createPublicGroupEvent(),
                                  createPublicGroupEvent(),
                                  createPublicGroupEvent(),
                                  createPrivateGroupEvent());

        assertThat(projectionService.projectPublicGroups().size()).isEqualTo(3);
    }

    //TODO: ProjectionServiceTest
    @Disabled
    @Test
    void getAllGroupWithVisibilityPublic_UserInGroup() {
        eventStoreService.saveAll(createPublicGroupEvent(uuidMock(0)),
                                  addUserEvent(uuidMock(0), "kobold"),
                                  createPrivateGroupEvent(),
                                  createPublicGroupEvent());

        assertThat(projectionService.projectPublicGroups()).hasSize(1);
        assertThat(projectionService.projectPublicGroups()).hasSize(2);
    }

    //TODO: ProjectionServiceTest
    @Test
    void getAllLecturesWithVisibilityPublic() {
        eventStoreService.saveAll(createLectureEvent(),
                                  createPublicGroupEvent(),
                                  createLectureEvent(),
                                  createLectureEvent(),
                                  createLectureEvent());

        assertThat(projectionService.projectLectures().size()).isEqualTo(4);
    }

    //TODO: SearchServiceTest
    @Test
    void findGroupWith_UserMember_AllGroups() {
        eventStoreService.saveAll(createPublicGroupEvent(uuidMock(0)),
                                  addUserEvent(uuidMock(0), "jens"),
                                  updateGroupTitleEvent(uuidMock(0)),
                                  updateGroupDescriptionEvent(uuidMock(0)));

        assertThat(searchService.searchPublicGroups("", new User("jens"))).isEmpty();
    }

    //TODO: SearchServiceTest
    @Test
    void findGroupWith_UserNoMember_AllGroups() {
        eventStoreService.saveAll(completePublicGroups(10, 0),
                                  completePrivateGroups(10, 0));

        assertThat(searchService.searchPublicGroups("", new User("jens"))).hasSize(10);
    }

    //TODO: SearchServiceTest
    @Test
    void findGroupWith_FilterGroups() {
        eventStoreService.saveAll(createPublicGroupEvent(uuidMock(0)),
                                  updateGroupTitleEvent(uuidMock(0), "KK"),
                                  updateGroupDescriptionEvent(uuidMock(0), "ABCDE"),
                                  createPublicGroupEvent(uuidMock(1)),
                                  updateGroupTitleEvent(uuidMock(1), "ABCDEFG"),
                                  updateGroupDescriptionEvent(uuidMock(1), "KK"),
                                  createPrivateGroupEvent());

        assertThat(searchService.searchPublicGroups("A", new User("jesus"))).hasSize(2);
        assertThat(searchService.searchPublicGroups("F", new User("jesus"))).hasSize(1);
        assertThat(searchService.searchPublicGroups("Z", new User("jesus"))).hasSize(0);
    }

}
