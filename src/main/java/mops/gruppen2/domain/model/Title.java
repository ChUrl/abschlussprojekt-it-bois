package mops.gruppen2.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Value
public class Title {

    @NotBlank
    @Size(min = 4, max = 128)
    @JsonProperty("title")
    String groupTitle;

    @Override
    public String toString() {
        return groupTitle;
    }
}
