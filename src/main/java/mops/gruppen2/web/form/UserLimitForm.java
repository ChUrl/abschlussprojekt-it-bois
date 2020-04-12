package mops.gruppen2.web.form;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
public class UserLimitForm {

    @Min(1)
    @Max(999_999)
    long userlimit;
}
