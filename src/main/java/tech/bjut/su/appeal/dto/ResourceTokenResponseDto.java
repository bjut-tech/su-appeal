package tech.bjut.su.appeal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import org.springframework.lang.Nullable;
import tech.bjut.su.appeal.jsonview.UserViews;

@Data
@JsonView(UserViews.Public.class)
public class ResourceTokenResponseDto<T> {

    @JsonProperty("resource_token")
    private TokenResponseDto resourceToken;

    @Nullable
    private T data;
}
