package tech.bjut.su.appeal.service;

import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import tech.bjut.su.appeal.dto.AnnouncementCategoryCreateDto;
import tech.bjut.su.appeal.dto.AnnouncementCreateDto;
import tech.bjut.su.appeal.entity.Announcement;
import tech.bjut.su.appeal.entity.AnnouncementCategory;
import tech.bjut.su.appeal.entity.Attachment;
import tech.bjut.su.appeal.entity.User;
import tech.bjut.su.appeal.repository.AnnouncementCategoryRepository;
import tech.bjut.su.appeal.repository.AnnouncementRepository;
import tech.bjut.su.appeal.repository.AttachmentRepository;
import tech.bjut.su.appeal.util.CursorPagination;

import java.util.List;
import java.util.Optional;

@Service
public class AnnouncementService {

    private final AnnouncementRepository repository;

    private final AnnouncementCategoryRepository categoryRepository;

    private final AttachmentRepository attachmentRepository;

    public AnnouncementService(
        AnnouncementRepository repository,
        AnnouncementCategoryRepository categoryRepository,
        AttachmentRepository attachmentRepository
    ) {
        this.repository = repository;
        this.categoryRepository = categoryRepository;
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

    @Transactional
    public Announcement create(User user, AnnouncementCreateDto dto) {
        Announcement announcement = new Announcement();
        return update(announcement, user, dto);
    }

    @Transactional
    public Announcement update(Announcement announcement, User user, AnnouncementCreateDto dto) {
        announcement.setUser(user);
        announcement.setTitle(StringUtils.stripToEmpty(dto.getTitle()));
        announcement.setContent(StringUtils.stripToEmpty(dto.getContent()));

        if (dto.getCategoryId() != null) {
            AnnouncementCategory category = categoryRepository.findById(dto.getCategoryId()).orElse(null);
            announcement.setCategory(category);
        } else {
            announcement.setCategory(null);
        }

        if (dto.getAttachmentIds() != null && !dto.getAttachmentIds().isEmpty()) {
            List<Attachment> existingAttachments = attachmentRepository.findAllById(dto.getAttachmentIds());
            announcement.setAttachments(existingAttachments);
        } else {
            announcement.setAttachments(null);
        }

        return repository.save(announcement);
    }

    public void setPinned(Announcement announcement, boolean pinned) {
        announcement.setPinned(pinned);
        repository.save(announcement);
    }

    public void delete(long id) {
        repository.deleteById(id);
    }

    public void delete(Announcement announcement) {
        repository.delete(announcement);
    }

    public List<AnnouncementCategory> getCategories() {
        return categoryRepository.findAll();
    }

    public AnnouncementCategory getCategory(long id) {
        return categoryRepository.findById(id).orElseThrow();
    }

    public AnnouncementCategory createCategory(AnnouncementCategoryCreateDto dto) {
        AnnouncementCategory category = new AnnouncementCategory();

        category.setName(StringUtils.stripToEmpty(dto.getName()));
        category.setDescription(StringUtils.stripToEmpty(dto.getDescription()));

        return categoryRepository.save(category);
    }

    public AnnouncementCategory updateCategory(long id, AnnouncementCategoryCreateDto dto) {
        AnnouncementCategory category = categoryRepository.findById(id).orElseThrow();

        category.setName(StringUtils.stripToEmpty(dto.getName()));
        category.setDescription(StringUtils.stripToEmpty(dto.getDescription()));

        return categoryRepository.save(category);
    }

    public void deleteCategory(long id) {
        categoryRepository.deleteById(id);
    }
}
