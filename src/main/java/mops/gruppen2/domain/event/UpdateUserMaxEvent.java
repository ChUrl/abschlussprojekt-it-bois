package mops.gruppen2.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mops.gruppen2.domain.Group;
import mops.gruppen2.domain.exception.EventException;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserMaxEvent extends Event {

    private  Long userMaximum;

    public UpdateUserMaxEvent(Long group_id, String user_id, Long userMaximum) {
        super(group_id,user_id);
        this.userMaximum = userMaximum;
    }

    @Override
    protected void applyEvent(Group group) throws EventException {
        group.setUserMaximum(this.userMaximum);
    }
}