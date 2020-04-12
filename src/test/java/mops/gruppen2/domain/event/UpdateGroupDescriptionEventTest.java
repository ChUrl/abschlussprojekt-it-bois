package mops.gruppen2.domain.event;

import mops.gruppen2.TestBuilder;
import mops.gruppen2.domain.exception.BadParameterException;
import mops.gruppen2.domain.model.Group;
import org.junit.jupiter.api.Test;

import static mops.gruppen2.TestBuilder.createPublicGroupEvent;
import static mops.gruppen2.TestBuilder.uuidMock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UpdateGroupDescriptionEventTest {

    @Test
    void applyEvent() {
        Event createEvent = createPublicGroupEvent(uuidMock(0));
        Event updateEvent = new UpdateGroupDescriptionEvent(uuidMock(0), "A", "desc.");

        Group group = TestBuilder.apply(createEvent, updateEvent);

        assertThat(group.getDescription()).isEqualTo("desc.");
    }

    @Test
    void applyEvent_badDescription() {
        Event createEvent = createPublicGroupEvent(uuidMock(0));
        Event updateEventA = new UpdateGroupDescriptionEvent(uuidMock(0), "A", "");

        Group group = TestBuilder.apply(createEvent);

        assertThrows(BadParameterException.class, () -> updateEventA.apply(group));
    }
}
