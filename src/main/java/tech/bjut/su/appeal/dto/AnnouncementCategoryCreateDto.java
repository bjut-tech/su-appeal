package tech.bjut.su.appeal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AnnouncementCategoryCreateDto {

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 65535)
    private String description;
}
