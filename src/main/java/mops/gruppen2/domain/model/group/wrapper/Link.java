package mops.gruppen2.domain.model.group.wrapper;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Value
public class Link {

    @NotNull
    @Size(min = 36, max = 36)
    @JsonProperty("value")
    String value;

    public static Link RANDOM() {
        return new Link(UUID.randomUUID().toString());
    }
}
