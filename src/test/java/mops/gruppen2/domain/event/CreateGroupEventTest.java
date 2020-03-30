package mops.gruppen2.domain.event;

import mops.gruppen2.TestBuilder;
import mops.gruppen2.domain.Group;
import mops.gruppen2.domain.GroupType;
import mops.gruppen2.domain.Visibility;
import org.junit.jupiter.api.Test;

import static mops.gruppen2.TestBuilder.uuidFromInt;
import static org.assertj.core.api.Assertions.assertThat;

class CreateGroupEventTest {

    @Test
    void applyEvent() {
        Event createEvent = new CreateGroupEvent(uuidFromInt(0),
                                                 "A",
                                                 uuidFromInt(1),
                                                 GroupType.SIMPLE,
                                                 Visibility.PUBLIC,
                                                 100L);

        Group group = TestBuilder.apply(createEvent);

        assertThat(group.getMembers()).hasSize(0);
        assertThat(group.getType()).isEqualTo(GroupType.SIMPLE);
        assertThat(group.getVisibility()).isEqualTo(Visibility.PUBLIC);
        assertThat(group.getUserMaximum()).isEqualTo(100);
        assertThat(group.getId()).isEqualTo(uuidFromInt(0));
        assertThat(group.getParent()).isEqualTo(uuidFromInt(1));
    }
}
