package tech.bjut.su.appeal.dto;

import lombok.Data;
import org.springframework.lang.Nullable;

@Data
public class AnnouncementIndexDto {

    @Nullable
    private Long categoryId;

    @Nullable
    private String search;

    @Nullable
    private String cursor;
}
