package mops.gruppen2.domain.model.group.wrapper;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Value
public class Title {

    @NotNull
    @Size(min = 4, max = 128)
    @JsonProperty("value")
    String value;

    @Override
    public String toString() {
        return value;
    }
}
