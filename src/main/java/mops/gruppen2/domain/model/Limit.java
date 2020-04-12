package mops.gruppen2.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Value
public class Limit {

    @Min(1)
    @Max(999_999)
    @JsonProperty("limit")
    long userLimit;

    @Override
    public String toString() {
        return String.valueOf(userLimit);
    }
}
