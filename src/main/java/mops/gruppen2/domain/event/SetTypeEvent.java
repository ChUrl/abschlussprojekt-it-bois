package mops.gruppen2.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.extern.log4j.Log4j2;
import mops.gruppen2.domain.exception.EventException;
import mops.gruppen2.domain.model.group.Group;
import mops.gruppen2.domain.model.group.Type;
import mops.gruppen2.infrastructure.GroupCache;

import javax.validation.Valid;
import java.util.UUID;

@Log4j2
@Value
@AllArgsConstructor
public class SetTypeEvent extends Event {

    @JsonProperty("type")
    Type type;

    //TODO: blöder hack, das soll eigentlich anders gehen
    // Problem ist, dass die Gruppe vor dem Cache verändert wird, also kann der cache den alten Typ
    // nicht mehr aus der Gruppe holen
    @NonFinal
    Type oldType;

    public SetTypeEvent(UUID groupId, String exec, @Valid Type type) {
        super(groupId, exec, null);

        this.type = type;
    }

    @Override
    protected void updateCache(GroupCache cache, Group group) {
        cache.typesRemove(oldType, group);
        cache.typesPut(type, group);
    }

    @Override
    protected void applyEvent(Group group) throws EventException {
        oldType = group.getType();
        group.setType(exec, type);
    }

    @Override
    public String format() {
        return "Gruppentype gesetzt: " + type + ".";
    }

    @Override
    public String type() {
        return EventType.SETTYPE.toString();
    }
}
