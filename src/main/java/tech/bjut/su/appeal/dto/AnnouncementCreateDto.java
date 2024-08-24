package tech.bjut.su.appeal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import tech.bjut.su.appeal.repository.AnnouncementCategoryRepository;
import tech.bjut.su.appeal.validation.EntityExists;

import java.util.List;
import java.util.UUID;

@Data
public class AnnouncementCreateDto {

    @NotBlank
    @Size(max = 255)
    private String title;

    @JsonProperty("category")
    @EntityExists(repository = AnnouncementCategoryRepository.class, message = "{announcement-category.not-found}")
    private Long categoryId;

    @NotBlank
    @Size(max = 65535)
    private String content;

    private List<UUID> attachmentIds;
}
