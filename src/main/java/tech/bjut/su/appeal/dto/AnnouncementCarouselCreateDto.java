package tech.bjut.su.appeal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import tech.bjut.su.appeal.repository.AnnouncementRepository;
import tech.bjut.su.appeal.validation.EntityExists;

import java.util.UUID;

@Data
public class AnnouncementCarouselCreateDto {

    @NotBlank
    @EntityExists(repository = AnnouncementRepository.class, message = "{announcement.not-found}")
    @JsonProperty("announcement")
    private Long announcementId;

    @JsonProperty("cover")
    private UUID coverId;
}
