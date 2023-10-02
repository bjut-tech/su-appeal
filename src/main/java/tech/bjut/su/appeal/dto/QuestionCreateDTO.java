package tech.bjut.su.appeal.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class QuestionCreateDTO {
    @NotBlank
    private String content;

    private List<UUID> attachmentIds;
}
