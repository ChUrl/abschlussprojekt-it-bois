package mops.gruppen2.domain.model.group.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Value
public class Description {

    @NotNull
    @Size(min = 4, max = 512)
    @JsonProperty("value")
    String value;

    @Override
    public String toString() {
        return value;
    }

    @JsonIgnore
    public static Description EMPTY() {
        return new Description("EMPTY");
    }
}
