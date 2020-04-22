package mops.gruppen2.domain.model.group.wrapper;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Value
public class Link {

    @NotNull
    @JsonProperty("value")
    UUID value;

    public Link(String value) {
        this.value = UUID.fromString(value);
    }

    public static Link RANDOM() {
        return new Link(UUID.randomUUID().toString());
    }

    public String getValue() {
        return value.toString();
    }
}
