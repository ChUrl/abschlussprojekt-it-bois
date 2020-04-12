package mops.gruppen2.web.form;

import lombok.Data;
import mops.gruppen2.domain.GroupType;
import mops.gruppen2.domain.service.IdService;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.UUID;

@Data
public class CreateForm {

    @NotBlank
    @Size(min = 3, max = 128)
    String title;

    @NotBlank
    @Size(min = 3, max = 512)
    String description;

    @NotBlank
    String type;

    @NotBlank
    String parent;

    @Min(1)
    @Max(999_999)
    long userlimit;

    MultipartFile file;

    public GroupType getType() {
        return GroupType.valueOf(type);
    }

    public UUID getParent() {
        return getType() == GroupType.LECTURE ? IdService.emptyUUID() : IdService.stringToUUID(parent);
    }
}
