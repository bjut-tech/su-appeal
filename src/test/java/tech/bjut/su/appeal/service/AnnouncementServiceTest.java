package tech.bjut.su.appeal.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bjut.su.appeal.dto.AnnouncementCreateDto;
import tech.bjut.su.appeal.entity.Announcement;
import tech.bjut.su.appeal.entity.Attachment;
import tech.bjut.su.appeal.entity.User;
import tech.bjut.su.appeal.repository.AnnouncementRepository;
import tech.bjut.su.appeal.repository.AttachmentRepository;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnnouncementServiceTest {

    @Mock
    private AnnouncementRepository announcementRepository;

    @Mock
    private AttachmentRepository attachmentRepository;

    @InjectMocks
    private AnnouncementService announcementService;

    // The test data
    private User user;

    private static final long ANNOUNCEMENT_ID = 1;
    private static final String USER_UID = "user";
    private static final String DTO_TITLE = "title";
    private static final String DTO_CONTENT = "content";
    private static final UUID DTO_ATTACHMENT_ID = UUID.randomUUID();

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setUid(USER_UID);
    }

    @Test
    public void testCreate_noAttachment() {
        // set up mocking
        setUpMocking_announcementRepository_save();

        // execute
        AnnouncementCreateDto dto = new AnnouncementCreateDto();
        dto.setTitle(DTO_TITLE);
        dto.setContent(DTO_CONTENT);

        Announcement res = announcementService.create(user, dto);

        // verify
        assertThat(res).isNotNull();
        assertThat(res.getId()).isGreaterThan(0);
        assertThat(res.getUser()).isEqualTo(user);
        assertThat(res.getTitle()).isEqualTo(DTO_TITLE);
        assertThat(res.getContent()).isEqualTo(DTO_CONTENT);
        assertThat(res.getAttachments()).isNullOrEmpty();
        verify(announcementRepository, times(1)).save(any(Announcement.class));
    }

    @Test
    public void testCreate_oneAttachment() {
        // setup mocking
        setUpMocking_announcementRepository_save();
        setUpMocking_attachmentRepository_findAllById();

        // execute
        AnnouncementCreateDto dto = new AnnouncementCreateDto();
        dto.setTitle(DTO_TITLE);
        dto.setContent(DTO_CONTENT);
        dto.setAttachmentIds(List.of(DTO_ATTACHMENT_ID));

        Announcement res = announcementService.create(user, dto);

        // verify
        assertThat(res).isNotNull();
        assertThat(res.getAttachments()).hasSize(1);
        assertThat(res.getAttachments().get(0).getId()).isEqualTo(DTO_ATTACHMENT_ID);
        verify(announcementRepository, times(1)).save(any(Announcement.class));
        verify(attachmentRepository, times(1)).findAllById(anyList());
    }

    @Test
    public void testSetPinned_setToTrue() {
        // setup mocking
        setUpMocking_announcementRepository_save();

        // execute
        Announcement announcement = new Announcement();
        announcement.setPinned(false);

        announcementService.setPinned(announcement, true);

        // verify
        assertThat(announcement.isPinned()).isTrue();
        verify(announcementRepository, times(1)).save(announcement);
    }

    @Test
    public void testSetPinned_setToFalse() {
        // setup mocking
        setUpMocking_announcementRepository_save();

        // execute
        Announcement announcement = new Announcement();
        announcement.setPinned(true);

        announcementService.setPinned(announcement, false);

        // verify
        assertThat(announcement.isPinned()).isFalse();
        verify(announcementRepository, times(1)).save(announcement);
    }

    private void setUpMocking_announcementRepository_save() {
        when(announcementRepository.save(any(Announcement.class)))
            .thenAnswer(invocation -> {
                Announcement announcement = invocation.getArgument(0);
                announcement.setId(ANNOUNCEMENT_ID);
                return announcement;
            });
    }

    private void setUpMocking_attachmentRepository_findAllById() {
        when(attachmentRepository.findAllById(anyList()))
            .thenAnswer(invocation -> {
                List<UUID> ids = invocation.getArgument(0);
                return ids.stream().map(id -> {
                    Attachment attachment = new Attachment();
                    attachment.setId(id);
                    return attachment;
                }).toList();
            });
    }
}
