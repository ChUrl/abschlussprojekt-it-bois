package mops.gruppen2.domain.event;

import mops.gruppen2.TestBuilder;
import mops.gruppen2.domain.exception.UserNotFoundException;
import mops.gruppen2.domain.model.group.Group;
import org.junit.jupiter.api.Test;

import static mops.gruppen2.TestBuilder.addUserEvent;
import static mops.gruppen2.TestBuilder.createPublicGroupEvent;
import static mops.gruppen2.TestBuilder.updateUserLimitMaxEvent;
import static mops.gruppen2.TestBuilder.uuidMock;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KickMemberEventTest {

    @Test
    void applyEvent() {
        Event createEvent = createPublicGroupEvent(uuidMock(0));
        Event updateLimitEvent = updateUserLimitMaxEvent(uuidMock(0));
        Event addEvent = addUserEvent(uuidMock(0), "A");
        Event deleteEvent = new KickMemberEvent(uuidMock(0), "A");

        Group group = TestBuilder.apply(createEvent, updateLimitEvent, addEvent, deleteEvent);

        assertThat(group.getMemberships()).hasSize(0);
    }

    @Test
    void applyEvent_userNotFound() {
        Event createEvent = createPublicGroupEvent(uuidMock(0));
        Event updateLimitEvent = updateUserLimitMaxEvent(uuidMock(0));
        Event addEvent = addUserEvent(uuidMock(0), "A");
        Event deleteEvent = new KickMemberEvent(uuidMock(0), "B");

        Group group = TestBuilder.apply(createEvent, updateLimitEvent, addEvent);

        assertThrows(UserNotFoundException.class, () -> deleteEvent.apply(group));
        assertThat(group.getMemberships()).hasSize(1);
    }
}
