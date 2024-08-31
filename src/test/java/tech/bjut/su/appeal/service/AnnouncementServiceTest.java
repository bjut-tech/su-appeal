package tech.bjut.su.appeal.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import tech.bjut.su.appeal.dto.AnnouncementCarouselCreateDto;
import tech.bjut.su.appeal.dto.AnnouncementCreateDto;
import tech.bjut.su.appeal.entity.*;
import tech.bjut.su.appeal.enums.UserRoleEnum;
import tech.bjut.su.appeal.repository.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(AnnouncementService.class)
@Transactional
public class AnnouncementServiceTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private AnnouncementCategoryRepository announcementCategoryRepository;

    @Autowired
    private AnnouncementCarouselRepository announcementCarouselRepository;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AnnouncementService announcementService;

    // The test data
    private User user;

    private static final String USER_UID = "user1";
    private static final String ANNOUNCEMENT_TITLE = "title";
    private static final String ANNOUNCEMENT_CONTENT = "content";
    private static final String CATEGORY_NAME = "category";

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setUid(USER_UID);
        user.setRole(UserRoleEnum.STUDENT);

        user = userRepository.save(user);
    }

    @Test
    public void testCreate_noAttachment() {
        // execute
        AnnouncementCreateDto dto = new AnnouncementCreateDto();
        dto.setTitle(ANNOUNCEMENT_TITLE);
        dto.setContent(ANNOUNCEMENT_CONTENT);

        Announcement announcement = announcementService.create(user, dto);

        // flush transaction
        entityManager.flush();
        entityManager.clear();

        // fetch and verify
        Announcement fetchedAnnouncement = announcementRepository.findById(announcement.getId()).orElse(null);
        assertThat(fetchedAnnouncement).isNotNull();
        assertThat(fetchedAnnouncement.getUser()).isEqualTo(user);
        assertThat(fetchedAnnouncement.getCategory()).isNull();
        assertThat(fetchedAnnouncement.getTitle()).isEqualTo(ANNOUNCEMENT_TITLE);
        assertThat(fetchedAnnouncement.getContent()).isEqualTo(ANNOUNCEMENT_CONTENT);
        assertThat(fetchedAnnouncement.getAttachments()).isNullOrEmpty();
        assertThat(fetchedAnnouncement.isPinned()).isFalse(); // should not be pinned by default
        assertThat(fetchedAnnouncement.isHidden()).isFalse(); // should not be hidden by default
    }

    @Test
    public void testCreate_canSaveAttachments() {
        // setup
        Attachment attachment = new Attachment();
        attachment.setSize(0);
        attachment = attachmentRepository.save(attachment);

        AnnouncementCreateDto dto = new AnnouncementCreateDto();
        dto.setTitle(ANNOUNCEMENT_TITLE);
        dto.setContent(ANNOUNCEMENT_CONTENT);
        dto.setAttachmentIds(List.of(attachment.getId()));

        Announcement announcement = announcementService.create(user, dto);

        // flush transaction
        entityManager.flush();
        entityManager.clear();

        // fetch and verify
        Announcement fetchedAnnouncement = announcementRepository.findById(announcement.getId()).orElse(null);
        assertThat(fetchedAnnouncement).isNotNull();
        assertThat(fetchedAnnouncement.getUser()).isEqualTo(user);
        assertThat(fetchedAnnouncement.getCategory()).isNull();
        assertThat(fetchedAnnouncement.getTitle()).isEqualTo(ANNOUNCEMENT_TITLE);
        assertThat(fetchedAnnouncement.getContent()).isEqualTo(ANNOUNCEMENT_CONTENT);
        assertThat(fetchedAnnouncement.getAttachments()).containsExactly(attachment);
        assertThat(fetchedAnnouncement.isPinned()).isFalse(); // should not be pinned by default
        assertThat(fetchedAnnouncement.isHidden()).isFalse(); // should not be hidden by default
    }

    @Test
    public void testSetPinned_setToTrue() {
        // setup
        Announcement announcement = new Announcement();
        announcement.setUser(user);
        announcement.setTitle(ANNOUNCEMENT_TITLE);
        announcement.setContent(ANNOUNCEMENT_CONTENT);
        announcement.setPinned(false);
        announcementRepository.save(announcement);

        // flush transaction
        entityManager.flush();
        entityManager.clear();

        // execute
        announcementService.setPinned(announcement, true);

        // flush transaction
        entityManager.flush();
        entityManager.clear();

        // verify
        Announcement fetchedAnnouncement = announcementRepository.findById(announcement.getId()).orElse(null);
        assertThat(fetchedAnnouncement).isNotNull();
        assertThat(fetchedAnnouncement.isPinned()).isTrue();
    }

    @Test
    public void testSetPinned_setToFalse() {
        // setup
        Announcement announcement = new Announcement();
        announcement.setUser(user);
        announcement.setTitle(ANNOUNCEMENT_TITLE);
        announcement.setContent(ANNOUNCEMENT_CONTENT);
        announcement.setPinned(true);
        announcementRepository.save(announcement);

        // flush transaction
        entityManager.flush();
        entityManager.clear();

        // execute
        announcementService.setPinned(announcement, false);

        // flush transaction
        entityManager.flush();
        entityManager.clear();

        // verify
        Announcement fetchedAnnouncement = announcementRepository.findById(announcement.getId()).orElse(null);
        assertThat(fetchedAnnouncement).isNotNull();
        assertThat(fetchedAnnouncement.isPinned()).isFalse();
    }

    @Test
    public void testDelete() {
        // setup
        Announcement announcement = new Announcement();
        announcement.setUser(user);
        announcement.setTitle(ANNOUNCEMENT_TITLE);
        announcement.setContent(ANNOUNCEMENT_CONTENT);
        announcement = announcementRepository.save(announcement);

        AnnouncementCarousel carousel1 = new AnnouncementCarousel();
        carousel1.setAnnouncement(announcement);
        carousel1 = announcementCarouselRepository.save(carousel1);

        AnnouncementCarousel carousel2 = new AnnouncementCarousel();
        carousel2.setAnnouncement(announcement);
        carousel2 = announcementCarouselRepository.save(carousel2);

        // flush transaction
        entityManager.flush();
        entityManager.clear();

        // execute
        announcementService.delete(announcement);

        // flush transaction
        entityManager.flush();
        entityManager.clear();

        // verify
        Announcement fetchedAnnouncement = announcementRepository.findById(announcement.getId()).orElse(null);
        assertThat(fetchedAnnouncement).isNull();

        AnnouncementCarousel fetchedCarousel1 = announcementCarouselRepository.findById(carousel1.getId()).orElse(null);
        assertThat(fetchedCarousel1).isNull();

        AnnouncementCarousel fetchedCarousel2 = announcementCarouselRepository.findById(carousel2.getId()).orElse(null);
        assertThat(fetchedCarousel2).isNull();
    }

    @Test
    public void testDeleteCategory() {
        // setup
        AnnouncementCategory category = new AnnouncementCategory();
        category.setName(CATEGORY_NAME);
        category = announcementCategoryRepository.save(category);

        Announcement announcement = new Announcement();
        announcement.setUser(user);
        announcement.setTitle(ANNOUNCEMENT_TITLE);
        announcement.setContent(ANNOUNCEMENT_CONTENT);
        announcement.setCategory(category);
        announcement = announcementRepository.save(announcement);

        // flush transaction
        entityManager.flush();
        entityManager.clear();

        // execute
        announcementService.deleteCategory(category);

        // flush transaction
        entityManager.flush();
        entityManager.clear();

        // verify
        AnnouncementCategory fetchedCategory = announcementCategoryRepository.findById(category.getId()).orElse(null);
        assertThat(fetchedCategory).isNull();

        Announcement fetchedAnnouncement = announcementRepository.findById(announcement.getId()).orElse(null);
        assertThat(fetchedAnnouncement).isNotNull();
        assertThat(fetchedAnnouncement.getCategory()).isNull();
    }

    @Test
    public void testCreateCarousel() {
        // setup
        Announcement announcement = new Announcement();
        announcement.setUser(user);
        announcement.setTitle(ANNOUNCEMENT_TITLE);
        announcement.setContent(ANNOUNCEMENT_CONTENT);
        announcement = announcementRepository.save(announcement);

        // flush transaction
        entityManager.flush();
        entityManager.clear();

        // execute
        AnnouncementCarouselCreateDto dto = new AnnouncementCarouselCreateDto();
        dto.setAnnouncementId(announcement.getId());
        AnnouncementCarousel carousel = announcementService.createCarousel(dto);

        // flush transaction
        entityManager.flush();
        entityManager.clear();

        // verify
        AnnouncementCarousel fetchedCarousel = announcementCarouselRepository.findById(carousel.getId()).orElse(null);
        assertThat(fetchedCarousel).isNotNull();
        assertThat(fetchedCarousel.getAnnouncement()).isEqualTo(announcement);

        Announcement fetchedAnnouncement = announcementRepository.findById(announcement.getId()).orElse(null);
        assertThat(fetchedAnnouncement).isNotNull();
        assertThat(fetchedAnnouncement.isHidden()).isTrue();
    }

    @Test
    public void testDeleteCarousel() {
        // setup
        Announcement announcement = new Announcement();
        announcement.setUser(user);
        announcement.setTitle(ANNOUNCEMENT_TITLE);
        announcement.setContent(ANNOUNCEMENT_CONTENT);
        announcement.setHidden(true);
        announcement = announcementRepository.save(announcement);

        AnnouncementCarousel carousel = new AnnouncementCarousel();
        carousel.setAnnouncement(announcement);
        carousel = announcementCarouselRepository.save(carousel);

        // flush transaction
        entityManager.flush();
        entityManager.clear();

        // execute
        announcementService.deleteCarousel(carousel);

        // flush transaction
        entityManager.flush();
        entityManager.clear();

        // verify
        AnnouncementCarousel fetchedCarousel = announcementCarouselRepository.findById(carousel.getId()).orElse(null);
        assertThat(fetchedCarousel).isNull();

        Announcement fetchedAnnouncement = announcementRepository.findById(announcement.getId()).orElse(null);
        assertThat(fetchedAnnouncement).isNotNull();
        assertThat(fetchedAnnouncement.isHidden()).isFalse();
    }
}
