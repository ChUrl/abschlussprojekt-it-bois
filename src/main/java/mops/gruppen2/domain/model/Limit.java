package mops.gruppen2.domain.model;

import lombok.Value;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Value
//@Getter
//@AllArgsConstructor
//@NoArgsConstructor
public class Limit {

    //private Limit() {}

    @Min(1)
    @Max(999_999)
    long userLimit;
}
