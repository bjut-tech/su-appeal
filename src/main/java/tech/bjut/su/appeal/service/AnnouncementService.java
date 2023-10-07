package tech.bjut.su.appeal.service;

import org.springframework.stereotype.Service;
import tech.bjut.su.appeal.dto.AnnouncementCreateDto;
import tech.bjut.su.appeal.entity.Announcement;
import tech.bjut.su.appeal.entity.Attachment;
import tech.bjut.su.appeal.entity.User;
import tech.bjut.su.appeal.repository.AnnouncementRepository;
import tech.bjut.su.appeal.repository.AttachmentRepository;

import java.util.List;

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
