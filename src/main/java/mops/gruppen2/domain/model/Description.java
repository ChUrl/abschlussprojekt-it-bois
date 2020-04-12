package mops.gruppen2.domain.model;

import lombok.Getter;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Value
@Getter
//@AllArgsConstructor
//@NoArgsConstructor
public class Description {

    //private Description() {}

    @NotBlank
    @Size(min = 4, max = 512)
    String groupDescription;
}
