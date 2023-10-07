package tech.bjut.su.appeal.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import tech.bjut.su.appeal.dto.AnnouncementShowDto;
import tech.bjut.su.appeal.entity.Announcement;

@Component
public class AnnouncementShowConverter implements Converter<Announcement, AnnouncementShowDto> {

    @Override
    public AnnouncementShowDto convert(Announcement source) {
        AnnouncementShowDto dto = new AnnouncementShowDto();

        dto.setId(source.getId());
        dto.setTitle(source.getTitle());
        dto.setContent(source.getContent());
        dto.setAttachments(source.getAttachments());
        dto.setCreatedAt(source.getCreatedAt());

        return dto;
    }
}
