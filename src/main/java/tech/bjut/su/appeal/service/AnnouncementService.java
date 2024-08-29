package tech.bjut.su.appeal.service;

import jakarta.persistence.criteria.Expression;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import tech.bjut.su.appeal.dto.AnnouncementCarouselCreateDto;
import tech.bjut.su.appeal.dto.AnnouncementCategoryCreateDto;
import tech.bjut.su.appeal.dto.AnnouncementCreateDto;
import tech.bjut.su.appeal.dto.AnnouncementIndexDto;
import tech.bjut.su.appeal.entity.*;
import tech.bjut.su.appeal.repository.AnnouncementCarouselRepository;
import tech.bjut.su.appeal.repository.AnnouncementCategoryRepository;
import tech.bjut.su.appeal.repository.AnnouncementRepository;
import tech.bjut.su.appeal.repository.AttachmentRepository;
import tech.bjut.su.appeal.util.CursorPaginationHelper;

import java.util.List;

@Service
public class AnnouncementService {

    private final AnnouncementRepository repository;

    private final AnnouncementCarouselRepository carouselRepository;

    private final AnnouncementCategoryRepository categoryRepository;

    private final AttachmentRepository attachmentRepository;

    public AnnouncementService(
        AnnouncementRepository repository,
        AnnouncementCarouselRepository carouselRepository,
        AnnouncementCategoryRepository categoryRepository,
        AttachmentRepository attachmentRepository
    ) {
        this.repository = repository;
        this.carouselRepository = carouselRepository;
        this.categoryRepository = categoryRepository;
        this.attachmentRepository = attachmentRepository;
    }

    public Window<Announcement> index(AnnouncementIndexDto dto) {
        KeysetScrollPosition position = CursorPaginationHelper.positionOf(dto.getCursor(), true);
        Specification<Announcement> spec = Specification.allOf((root, query, builder) -> {
            if (dto.getCategoryId() == null) {
                return null;
            }
            return builder.equal(root.get("category").get("id"), dto.getCategoryId());
        }, (root, query, builder) -> {
            String search = StringUtils.trimToEmpty(dto.getSearch());
            if (search.isEmpty()) {
                return builder.equal(root.get("hidden"), false);
            }
            Expression<String> pattern = builder.concat(builder.concat(builder.literal("%"), search), builder.literal("%"));
            return builder.or(
                builder.like(root.get("title"), pattern),
                builder.like(root.get("content"), pattern)
            );
        });

        return repository.findAllPaginatedOrderByPinnedDescAndIdDesc(spec, 10, position);
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

    public void delete(Announcement announcement) {
        repository.delete(announcement);
    }

    public List<AnnouncementCategory> getCategories() {
        return categoryRepository.findAll();
    }

    public AnnouncementCategory createCategory(AnnouncementCategoryCreateDto dto) {
        AnnouncementCategory category = new AnnouncementCategory();

        category.setName(StringUtils.stripToEmpty(dto.getName()));
        category.setDescription(StringUtils.stripToEmpty(dto.getDescription()));

        return categoryRepository.save(category);
    }

    public AnnouncementCategory updateCategory(AnnouncementCategory category, AnnouncementCategoryCreateDto dto) {
        category.setName(StringUtils.stripToEmpty(dto.getName()));
        category.setDescription(StringUtils.stripToEmpty(dto.getDescription()));

        return categoryRepository.save(category);
    }

    public void deleteCategory(AnnouncementCategory category) {
        categoryRepository.delete(category);
    }

    public List<AnnouncementCarousel> getCarousels() {
        return carouselRepository.findAll();
    }

    @Transactional
    public AnnouncementCarousel createCarousel(AnnouncementCarouselCreateDto dto) {
        AnnouncementCarousel carousel = new AnnouncementCarousel();

        Announcement announcement = repository.findById(dto.getAnnouncementId()).orElseThrow();
        announcement.setHidden(true);
        announcement = repository.save(announcement);
        carousel.setAnnouncement(announcement);

        if (dto.getCoverId() != null) {
            Attachment cover = attachmentRepository.findById(dto.getCoverId()).orElse(null);
            carousel.setCover(cover);
        } else {
            carousel.setCover(null);
        }

        return carouselRepository.save(carousel);
    }

    @Transactional
    public void deleteCarousel(AnnouncementCarousel carousel) {
        Announcement announcement = carousel.getAnnouncement();
        announcement.setHidden(false);
        repository.save(announcement);

        carouselRepository.delete(carousel);
    }
}
