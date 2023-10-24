package tech.bjut.su.appeal.service;

import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import tech.bjut.su.appeal.dto.AnnouncementCreateDto;
import tech.bjut.su.appeal.entity.Announcement;
import tech.bjut.su.appeal.entity.Attachment;
import tech.bjut.su.appeal.entity.User;
import tech.bjut.su.appeal.repository.AnnouncementRepository;
import tech.bjut.su.appeal.repository.AttachmentRepository;
import tech.bjut.su.appeal.util.CursorPagination;

import java.util.List;
import java.util.Optional;

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

    public List<Announcement> getPinned() {
        return repository.findByPinnedTrueOrderByIdDesc();
    }

    public Window<Announcement> getPaginated(@Nullable String cursor) {
        KeysetScrollPosition position = CursorPagination.positionOf(cursor);

        return repository.findFirst10ByPinnedFalseOrderByIdDesc(position);
    }

    public Optional<Announcement> find(Long id) {
        return repository.findById(id);
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

        return repository.saveAndFlush(announcement);
    }

    public void setPinned(Announcement announcement, boolean pinned) {
        announcement.setPinned(pinned);
        repository.saveAndFlush(announcement);
    }

    public void delete(long id) {
        repository.deleteById(id);
    }

    public void delete(Announcement announcement) {
        repository.delete(announcement);
    }
}
