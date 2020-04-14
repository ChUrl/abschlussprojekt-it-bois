package mops.gruppen2.domain.event;

import mops.gruppen2.TestBuilder;
import mops.gruppen2.domain.exception.BadArgumentException;
import mops.gruppen2.domain.model.group.Group;
import org.junit.jupiter.api.Test;

import static mops.gruppen2.TestBuilder.createPublicGroupEvent;
import static mops.gruppen2.TestBuilder.uuidMock;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SetTitleEventTest {

    @Test
    void applyEvent() {
        Event createEvent = createPublicGroupEvent(uuidMock(0));
        Event updateEvent = new SetTitleEvent(uuidMock(0), "A", "title.");

        Group group = TestBuilder.apply(createEvent, updateEvent);

        assertThat(group.getTitle()).isEqualTo("title.");
    }

    @Test
    void applyEvent_badDescription() {
        Event createEvent = createPublicGroupEvent(uuidMock(0));
        Event updateEventA = new SetTitleEvent(uuidMock(0), "A", "");

        Group group = TestBuilder.apply(createEvent);

        assertThrows(BadArgumentException.class, () -> updateEventA.apply(group));
    }
}
