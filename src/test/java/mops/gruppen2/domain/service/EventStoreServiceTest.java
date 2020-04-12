package mops.gruppen2.domain.service;

import mops.gruppen2.Gruppen2Application;
import mops.gruppen2.domain.User;
import mops.gruppen2.domain.event.Event;
import mops.gruppen2.persistance.EventRepository;
import mops.gruppen2.persistance.dto.EventDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static mops.gruppen2.TestBuilder.addUserEvent;
import static mops.gruppen2.TestBuilder.addUserEvents;
import static mops.gruppen2.TestBuilder.createPrivateGroupEvents;
import static mops.gruppen2.TestBuilder.createPublicGroupEvent;
import static mops.gruppen2.TestBuilder.createPublicGroupEvents;
import static mops.gruppen2.TestBuilder.uuidMock;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Gruppen2Application.class)
@Transactional
@Rollback
class EventStoreServiceTest {

    @Autowired
    private EventRepository eventRepository;
    private EventStoreService eventStoreService;
    @Autowired
    private JdbcTemplate template;

    @SuppressWarnings("SyntaxError")
    @BeforeEach
    void setUp() {
        eventStoreService = new EventStoreService(eventRepository);
        eventRepository.deleteAll();
        //noinspection SqlResolve
        template.execute("ALTER TABLE event ALTER COLUMN event_id RESTART WITH 1");
    }

    @Test
    void saveEvent() {
        eventStoreService.saveEvent(createPublicGroupEvent());

        assertThat(eventRepository.findAll()).hasSize(1);
    }

    @Test
    void saveAll() {
        eventStoreService.saveAll(createPrivateGroupEvents(10));

        assertThat(eventRepository.findAll()).hasSize(10);
    }

    @Test
    void testSaveAll() {
        eventStoreService.saveAll(createPublicGroupEvents(5),
                                  createPrivateGroupEvents(5));

        assertThat(eventRepository.findAll()).hasSize(10);
    }

    @Test
    void getDTO() {
        Event event = createPublicGroupEvent();

        EventDTO dto = EventStoreService.getDTOFromEvent(event);

        assertThat(dto.getGroup_id()).isEqualTo(event.getGroupId().toString());
        assertThat(dto.getUser_id()).isEqualTo(event.getUserId());
        assertThat(dto.getEvent_id()).isEqualTo(null);
        assertThat(dto.getEvent_type()).isEqualTo("CreateGroupEvent");
    }

    @Test
    void getEventsOfGroup() {
        eventStoreService.saveAll(addUserEvents(10, uuidMock(0)),
                                  addUserEvents(5, uuidMock(1)));

        assertThat(eventStoreService.findGroupEvents(uuidMock(0))).hasSize(10);
        assertThat(eventStoreService.findGroupEvents(uuidMock(1))).hasSize(5);
    }

    @Test
    void findGroupIdsByUser() {
        eventStoreService.saveAll(addUserEvent(uuidMock(0), "A"),
                                  addUserEvent(uuidMock(1), "A"),
                                  addUserEvent(uuidMock(2), "A"),
                                  addUserEvent(uuidMock(3), "A"),
                                  addUserEvent(uuidMock(3), "B"));

        assertThat(eventStoreService.findExistingUserGroups(new User("A"))).hasSize(4);
        assertThat(eventStoreService.findExistingUserGroups(new User("B"))).hasSize(1);
    }
}
