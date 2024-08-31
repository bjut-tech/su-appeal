package tech.bjut.su.appeal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.bjut.su.appeal.entity.AnnouncementCarousel;

import java.util.List;

public interface AnnouncementCarouselRepository extends JpaRepository<AnnouncementCarousel, Long> {

    void deleteAllByAnnouncementId(Long announcementId);

    @Query(value = "SELECT DISTINCT a.id FROM AnnouncementCarousel p JOIN p.cover a")
    List<String> findAllAttachmentIdsUsed();
}
