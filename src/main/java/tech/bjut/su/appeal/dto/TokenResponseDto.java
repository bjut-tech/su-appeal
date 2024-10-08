package tech.bjut.su.appeal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import tech.bjut.su.appeal.jsonview.UserViews;

@Data
@JsonView(UserViews.Public.class)
public class TokenResponseDto {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private long expiresIn;

    @JsonProperty("token_type")
    private String tokenType = "Bearer";
}
