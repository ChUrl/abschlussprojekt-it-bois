package mops.gruppen2.domain.event;

import mops.gruppen2.TestBuilder;
import mops.gruppen2.domain.model.Group;
import mops.gruppen2.domain.model.Type;
import org.junit.jupiter.api.Test;

import static mops.gruppen2.TestBuilder.uuidMock;
import static org.assertj.core.api.Assertions.assertThat;

class CreateGroupEventTest {

    @Test
    void applyEvent() {
        Event createEvent = new CreateGroupEvent(uuidMock(0),
                                                 "A",
                                                 uuidMock(1),
                                                 Type.PUBLIC);

        Group group = TestBuilder.apply(createEvent);

        assertThat(group.getMembers()).hasSize(0);
        assertThat(group.getType()).isEqualTo(Type.PUBLIC);
        assertThat(group.getGroupid()).isEqualTo(uuidMock(0));
        assertThat(group.getParent()).isEqualTo(uuidMock(1));
    }
}
