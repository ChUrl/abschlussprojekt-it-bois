package mops.gruppen2.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.exception.NoAccessException;
import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.domain.model.group.wrapper.Link;

import javax.validation.Valid;

@Log4j2
@Value
@AllArgsConstructor
public class SetInviteLinkEvent extends Event {

    @JsonProperty("link")
    Link link;

    public SetInviteLinkEvent(Group group, String exec, @Valid Link link) {
        super(group.getId(), exec, null);
        this.link = link;
    }

    @Override
    protected void applyEvent(Group group) throws NoAccessException {
        group.setLink(exec, link);

        log.trace("\t\t\t\t\tNeuer Link: {}", group.getLink());
    }

    @Override
    public String type() {
        return EventType.SETLINK.toString();
    }
}
