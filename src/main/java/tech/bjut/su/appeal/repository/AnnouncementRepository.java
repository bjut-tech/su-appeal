package tech.bjut.su.appeal.repository;

import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.bjut.su.appeal.entity.Announcement;

import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    @EntityGraph(attributePaths = {"user", "attachments"})
    List<Announcement> findByPinnedTrueAndHiddenFalseOrderByIdDesc();

    Window<Announcement> findFirst10ByPinnedFalseAndHiddenFalseOrderByIdDesc(KeysetScrollPosition position);

    @Query(value = "SELECT DISTINCT a.id FROM Announcement p JOIN p.attachments a")
    List<String> findAllAttachmentIdsUsed();
}
