package mops.gruppen2.web.form;

import lombok.Data;
import mops.gruppen2.domain.GroupType;
import mops.gruppen2.domain.service.IdService;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import java.util.UUID;

@Data
public class CreateForm {

    @NotBlank
    String type;

    @NotBlank
    String parent;

    MultipartFile file;

    public GroupType getType() {
        return GroupType.valueOf(type);
    }

    public UUID getParent() {
        return getType() == GroupType.LECTURE ? IdService.emptyUUID() : IdService.stringToUUID(parent);
    }
}
