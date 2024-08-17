package tech.bjut.su.appeal.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import tech.bjut.su.appeal.dto.AnnouncementCreateDto;
import tech.bjut.su.appeal.entity.Announcement;
import tech.bjut.su.appeal.entity.Attachment;
import tech.bjut.su.appeal.entity.User;
import tech.bjut.su.appeal.enums.UserRoleEnum;
import tech.bjut.su.appeal.repository.AnnouncementRepository;
import tech.bjut.su.appeal.repository.AttachmentRepository;
import tech.bjut.su.appeal.repository.UserRepository;

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
        assertThat(fetchedAnnouncement.getTitle()).isEqualTo(ANNOUNCEMENT_TITLE);
        assertThat(fetchedAnnouncement.getContent()).isEqualTo(ANNOUNCEMENT_CONTENT);
        assertThat(fetchedAnnouncement.getAttachments()).isNullOrEmpty();
        assertThat(fetchedAnnouncement.isPinned()).isFalse(); // should not be pinned by default
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
        assertThat(fetchedAnnouncement.getTitle()).isEqualTo(ANNOUNCEMENT_TITLE);
        assertThat(fetchedAnnouncement.getContent()).isEqualTo(ANNOUNCEMENT_CONTENT);
        assertThat(fetchedAnnouncement.getAttachments()).containsExactly(attachment);
        assertThat(fetchedAnnouncement.isPinned()).isFalse(); // should not be pinned by default
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
    public void testDelete_byId() {
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
        announcementService.delete(announcement.getId());

        // flush transaction
        entityManager.flush();
        entityManager.clear();

        // verify
        Announcement fetchedAnnouncement = announcementRepository.findById(announcement.getId()).orElse(null);
        assertThat(fetchedAnnouncement).isNull();
    }

    @Test
    public void testDelete_byEntity() {
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
        announcementService.delete(announcement);

        // flush transaction
        entityManager.flush();
        entityManager.clear();

        // verify
        Announcement fetchedAnnouncement = announcementRepository.findById(announcement.getId()).orElse(null);
        assertThat(fetchedAnnouncement).isNull();
    }
}
