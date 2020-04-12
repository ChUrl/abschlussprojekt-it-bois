package mops.gruppen2.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Value
public class Description {

    @NotBlank
    @Size(min = 4, max = 512)
    @JsonProperty("desc")
    String groupDescription;

    @Override
    public String toString() {
        return groupDescription;
    }
}
