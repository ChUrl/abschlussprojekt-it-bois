package mops.gruppen2.domain.event;

import mops.gruppen2.TestBuilder;
import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.domain.model.group.Type;
import org.junit.jupiter.api.Test;

import static mops.gruppen2.TestBuilder.uuidMock;
import static org.assertj.core.api.Assertions.assertThat;

class DestroyGroupEventTest {

    @Test
    void applyEvent() {
        Event createEvent = new CreateGroupEvent(uuidMock(0),
                                                 "A",
                                                 uuidMock(1),
                                                 Type.PUBLIC);
        Event deleteEvent = new DestroyGroupEvent(uuidMock(0), "A");

        Group group = TestBuilder.apply(createEvent, deleteEvent);

        assertThat(group.getMemberships()).isEmpty();
        assertThat(group.getType()).isEqualTo(null);
        assertThat(group.getLimit()).isEqualTo(0);
        assertThat(group.getGroupid()).isEqualTo(uuidMock(0));
        assertThat(group.getParent()).isEqualTo(null);
    }
}
