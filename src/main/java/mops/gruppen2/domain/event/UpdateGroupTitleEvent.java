package mops.gruppen2.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.model.Group;
import mops.gruppen2.domain.model.Title;
import mops.gruppen2.domain.model.User;

/**
 * Ändert nur den Gruppentitel.
 */
@Log4j2
@Value
@AllArgsConstructor
public class UpdateGroupTitleEvent extends Event {

    @JsonProperty("title")
    Title title;

    public UpdateGroupTitleEvent(Group group, User user, Title title) {
        super(group.getGroupid(), user.getUserid());
        this.title = title;
    }

    @Override
    protected void applyEvent(Group group) {
        group.setTitle(title);

        log.trace("\t\t\t\t\tNeuer Titel: {}", group.getTitle());
    }

}
