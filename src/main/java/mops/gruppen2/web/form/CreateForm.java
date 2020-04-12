package mops.gruppen2.web.form;

import lombok.Data;
import mops.gruppen2.domain.helper.IdHelper;
import mops.gruppen2.domain.model.Type;
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

    public Type getType() {
        return Type.valueOf(type);
    }

    public UUID getParent() {
        return getType() == Type.LECTURE ? IdHelper.emptyUUID() : IdHelper.stringToUUID(parent);
    }
}
