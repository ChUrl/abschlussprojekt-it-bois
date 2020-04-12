package mops.gruppen2.domain.model;

import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Value
//@Getter
//@AllArgsConstructor
//@NoArgsConstructor
public class Title {

    //private Title() {}

    @NotBlank
    @Size(min = 4, max = 128)
    String groupTitle;
}
