package tech.bjut.su.appeal.dto;

import lombok.Data;
import tech.bjut.su.appeal.entity.Attachment;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AnnouncementShowDto {

    private long id;

    private String title;

    private String content;

    private List<Attachment> attachments;

    private LocalDateTime createdAt;
}
