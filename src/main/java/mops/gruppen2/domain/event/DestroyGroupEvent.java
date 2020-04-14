package mops.gruppen2.domain.event;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.exception.NoAccessException;
import mops.gruppen2.domain.model.group.Group;

@Log4j2
@Value
@AllArgsConstructor
public class DestroyGroupEvent extends Event {

    public DestroyGroupEvent(Group group, String exec) {
        super(group.getId(), exec, null);
    }

    @Override
    protected void applyEvent(Group group) throws NoAccessException {
        group.destroy(exec);

        log.trace("\t\t\t\t\tGelöschte Gruppe: {}", group.toString());
    }

    @Override
    public String type() {
        return EventType.DESTROYGROUP.toString();
    }
}
