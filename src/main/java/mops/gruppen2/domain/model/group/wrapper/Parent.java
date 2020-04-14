package mops.gruppen2.domain.model.group.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import mops.gruppen2.domain.helper.CommonHelper;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.beans.ConstructorProperties;
import java.util.UUID;

@Value
public class Parent {

    @NotNull
    @JsonProperty("id")
    UUID groupid;

    @ConstructorProperties("id")
    public Parent(@NotBlank @Size(min = 36, max = 36) String parentid) {
        groupid = UUID.fromString(parentid);
    }

    public static Parent EMPTY() {
        return new Parent("00000000-0000-0000-0000-000000000000");
    }

    @JsonIgnore
    public boolean isEmpty() {
        return CommonHelper.uuidIsEmpty(groupid);
    }
}
