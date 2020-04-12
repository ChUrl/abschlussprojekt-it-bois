package mops.gruppen2.web.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class MetaForm {

    @NotBlank
    @Size(min = 4, max = 128)
    String title;

    @NotBlank
    @Size(min = 4, max = 512)
    String description;
}
