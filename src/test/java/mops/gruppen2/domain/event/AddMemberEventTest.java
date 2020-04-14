package mops.gruppen2.domain.event;

import mops.gruppen2.domain.exception.GroupFullException;
import mops.gruppen2.domain.exception.UserAlreadyExistsException;
import mops.gruppen2.domain.model.group.Group;
import org.junit.jupiter.api.Test;

import static mops.gruppen2.TestBuilder.addUserEvent;
import static mops.gruppen2.TestBuilder.apply;
import static mops.gruppen2.TestBuilder.createPublicGroupEvent;
import static mops.gruppen2.TestBuilder.updateUserLimitMaxEvent;
import static mops.gruppen2.TestBuilder.uuidMock;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AddMemberEventTest {

    @Test
    void applyEvent() {
        Event createEvent = createPublicGroupEvent(uuidMock(0));
        Event updateLimitEvent = updateUserLimitMaxEvent(uuidMock(0));
        Event addEvent = new AddMemberEvent(uuidMock(0), "A", "Thomas", "Tom", "tho@mail.de");

        Group group = apply(createEvent, updateLimitEvent, addEvent);

        assertThat(group.getMemberships()).hasSize(1);
        assertThat(group.getMemberships().get(0).getGivenname()).isEqualTo("Thomas");
        assertThat(group.getMemberships().get(0).getFamilyname()).isEqualTo("Tom");
        assertThat(group.getMemberships().get(0).getEmail()).isEqualTo("tho@mail.de");
    }

    @Test
    void applyEvent_userAlreadyExists() {
        Event createEvent = createPublicGroupEvent(uuidMock(0));
        Event updateLimitEvent = updateUserLimitMaxEvent(uuidMock(0));
        Event addEventA = addUserEvent(uuidMock(0), "A");
        Event addEventB = addUserEvent(uuidMock(0), "B");
        Event addEventC = addUserEvent(uuidMock(0), "A");

        Group group = apply(createEvent, updateLimitEvent, addEventA, addEventB);

        assertThrows(UserAlreadyExistsException.class, () -> addEventA.apply(group));
        assertThrows(UserAlreadyExistsException.class, () -> addEventC.apply(group));
        assertThat(group.getMemberships()).hasSize(2);
    }

    @Test
    void applyEvent_groupFull() {
        Event createEvent = createPublicGroupEvent(uuidMock(0));
        Event maxSizeEvent = new SetLimitEvent(uuidMock(0), "A", 2L);
        Event addEventA = addUserEvent(uuidMock(0), "A");
        Event addEventB = addUserEvent(uuidMock(0), "B");
        Event addEventC = addUserEvent(uuidMock(0), "C");

        Group group = apply(createEvent, maxSizeEvent, addEventA, addEventB);

        assertThrows(GroupFullException.class, () -> addEventC.apply(group));
        assertThat(group.getMemberships()).hasSize(2);
    }
}
