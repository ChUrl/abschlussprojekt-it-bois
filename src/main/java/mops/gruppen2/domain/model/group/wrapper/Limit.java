package mops.gruppen2.domain.model.group.wrapper;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Value
public class Limit {

    @NotNull
    @Min(1)
    @Max(999_999)
    @JsonProperty("value")
    long value;

    public static Limit DEFAULT() {
        return new Limit(1);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
