package mops.gruppen2.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.exception.NoAccessException;
import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.domain.model.group.wrapper.Link;
import mops.gruppen2.infrastructure.GroupCache;

import javax.validation.Valid;
import java.util.UUID;

@Log4j2
@Value
@AllArgsConstructor
public class SetInviteLinkEvent extends Event {

    @JsonProperty("link")
    Link link;

    public SetInviteLinkEvent(UUID groupId, String exec, @Valid Link link) {
        super(groupId, exec, null);
        this.link = link;
    }

    @Override
    protected void updateCache(GroupCache cache, Group group) {
        cache.linksRemove(group.getLink());
        cache.linksPut(link.getValue(), group);
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
