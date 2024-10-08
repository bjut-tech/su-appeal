package tech.bjut.su.appeal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import tech.bjut.su.appeal.enums.CampusEnum;

import java.util.List;
import java.util.UUID;

@Data
public class QuestionCreateDto {

    @Size(max = 16)
    private String uid;

    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String contact;

    @NotNull(message = "{jakarta.validation.constraints.NotBlank.message}")
    private CampusEnum campus;

    @NotBlank
    @Size(max = 65535)
    private String content;

    private List<UUID> attachmentIds;
}
