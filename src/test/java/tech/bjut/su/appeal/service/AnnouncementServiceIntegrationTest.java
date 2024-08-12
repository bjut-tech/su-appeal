package tech.bjut.su.appeal.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
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

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
public class AnnouncementServiceIntegrationTest {

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

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setUid("user1");
        user.setRole(UserRoleEnum.STUDENT);
        user = userRepository.save(user);
    }

    @Test
    public void testCreate_isNotPinnedByDefault() {
        // setup
        AnnouncementCreateDto dto = new AnnouncementCreateDto();
        dto.setTitle("title");
        dto.setContent("content");

        Announcement announcement = announcementService.create(user, dto);

        // clear transaction
        entityManager.flush();
        entityManager.clear();

        // fetch and verify
        Announcement fetchedAnnouncement = announcementRepository.findById(announcement.getId()).orElse(null);
        assertThat(fetchedAnnouncement).isNotNull();
        assertThat(fetchedAnnouncement.isPinned()).isFalse();
    }

    @Test
    public void testCreate_canSaveAttachments() {
        // setup
        Attachment attachment = new Attachment();
        attachment.setSize(0);
        attachment = attachmentRepository.save(attachment);

        AnnouncementCreateDto dto = new AnnouncementCreateDto();
        dto.setTitle("title");
        dto.setContent("content");
        dto.setAttachmentIds(List.of(attachment.getId()));

        Announcement announcement = announcementService.create(user, dto);

        // clear transaction
        entityManager.flush();
        entityManager.clear();

        // fetch and verify
        Announcement fetchedAnnouncement = announcementRepository.findById(announcement.getId()).orElse(null);
        assertThat(fetchedAnnouncement).isNotNull();
        assertThat(fetchedAnnouncement.getAttachments()).containsExactly(attachment);
    }

    @Test
    public void testDelete_byId() {
        // setup
        Announcement announcement = new Announcement();
        announcement.setUser(user);
        announcement.setTitle("title");
        announcement.setContent("content");
        announcement = announcementRepository.save(announcement);

        announcementService.delete(announcement.getId());

        // clear transaction
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
        announcement.setTitle("title");
        announcement.setContent("content");
        announcement = announcementRepository.save(announcement);

        announcementService.delete(announcement);

        // clear transaction
        entityManager.flush();
        entityManager.clear();

        // verify
        Announcement fetchedAnnouncement = announcementRepository.findById(announcement.getId()).orElse(null);
        assertThat(fetchedAnnouncement).isNull();
    }
}
