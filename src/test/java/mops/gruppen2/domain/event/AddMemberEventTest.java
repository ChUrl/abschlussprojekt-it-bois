package mops.gruppen2.domain.event;

import mops.gruppen2.TestHelper;
import mops.gruppen2.domain.exception.IdMismatchException;
import mops.gruppen2.domain.model.group.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AddMemberEventTest {

    @Test
    void userMismatch() {
        assertThatThrownBy(() -> new AddMemberEvent(TestHelper.uuid(1), "TEST", "TEST", new User("PETER")))
                .isInstanceOf(IdMismatchException.class);
    }
}
