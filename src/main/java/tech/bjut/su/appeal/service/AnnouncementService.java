package tech.bjut.su.appeal.service;

import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import tech.bjut.su.appeal.dto.AnnouncementCreateDto;
import tech.bjut.su.appeal.entity.Announcement;
import tech.bjut.su.appeal.entity.Attachment;
import tech.bjut.su.appeal.entity.User;
import tech.bjut.su.appeal.repository.AnnouncementRepository;
import tech.bjut.su.appeal.repository.AttachmentRepository;

import java.util.List;
import java.util.Map;

@Service
public class AnnouncementService {

    private final AnnouncementRepository repository;

    private final AttachmentRepository attachmentRepository;

    public AnnouncementService(
        AnnouncementRepository repository,
        AttachmentRepository attachmentRepository
    ) {
        this.repository = repository;
        this.attachmentRepository = attachmentRepository;
    }

    public Window<Announcement> getPaginated(@Nullable String cursor) {
        KeysetScrollPosition position;
        if (cursor == null) {
            position = ScrollPosition.keyset();
        } else {
            position = ScrollPosition.of(Map.of("id", cursor), ScrollPosition.Direction.FORWARD);
        }

        return repository.findFirst10ByOrderByIdDesc(position);
    }

    public Announcement create(User user, AnnouncementCreateDto dto) {
        Announcement announcement = new Announcement();
        announcement.setUser(user);
        announcement.setTitle(dto.getTitle());
        announcement.setContent(dto.getContent());

        if (dto.getAttachmentIds() != null && !dto.getAttachmentIds().isEmpty()) {
            List<Attachment> existingAttachments = attachmentRepository.findAllById(dto.getAttachmentIds());
            announcement.setAttachments(existingAttachments);
        }

        return this.store(announcement);
    }

    public Announcement store(Announcement announcement) {
        return repository.saveAndFlush(announcement);
    }
}
