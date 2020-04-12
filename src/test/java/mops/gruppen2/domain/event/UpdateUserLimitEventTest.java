package mops.gruppen2.domain.event;

import mops.gruppen2.domain.exception.BadParameterException;
import mops.gruppen2.domain.model.Group;
import org.junit.jupiter.api.Test;

import static mops.gruppen2.TestBuilder.addUserEvent;
import static mops.gruppen2.TestBuilder.apply;
import static mops.gruppen2.TestBuilder.createPublicGroupEvent;
import static mops.gruppen2.TestBuilder.uuidMock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UpdateUserLimitEventTest {

    @Test
    void applyEvent() {
        Event createEvent = createPublicGroupEvent(uuidMock(0));
        Event updateEvent = new UpdateUserLimitEvent(uuidMock(0), "A", 5L);

        Group group = apply(createEvent, updateEvent);

        assertThat(group.getLimit()).isEqualTo(5);
    }

    @Test
    void applyEvent_badParameter_negative() {
        Event createEvent = createPublicGroupEvent(uuidMock(0));
        Event updateEvent = new UpdateUserLimitEvent(uuidMock(0), "A", -5L);

        Group group = apply(createEvent);

        assertThrows(BadParameterException.class, () -> updateEvent.apply(group));
    }

    @Test
    void applyEvent_badParameter_tooSmall() {
        Event createEvent = createPublicGroupEvent(uuidMock(0));
        Event updateEventA = new UpdateUserLimitEvent(uuidMock(0), "A", 5L);
        Event addEventA = addUserEvent(uuidMock(0));
        Event addEventB = addUserEvent(uuidMock(0));
        Event addEventC = addUserEvent(uuidMock(0));
        Event updateEventB = new UpdateUserLimitEvent(uuidMock(0), "A", 2L);

        Group group = apply(createEvent, updateEventA, addEventA, addEventB, addEventC);

        assertThrows(BadParameterException.class, () -> updateEventB.apply(group));
    }
}
