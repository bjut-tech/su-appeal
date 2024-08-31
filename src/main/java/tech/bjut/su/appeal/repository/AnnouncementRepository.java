package tech.bjut.su.appeal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import tech.bjut.su.appeal.entity.Announcement;
import tech.bjut.su.appeal.entity.AnnouncementCategory;
import tech.bjut.su.appeal.repository.support.ExtendedSpecificationExecutor;

import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long>, ExtendedSpecificationExecutor<Announcement> {

    @Modifying
    @Query(value = "UPDATE Announcement a SET a.category = NULL WHERE a.category = ?1")
    void updateAllCategoryToNullByCategory(AnnouncementCategory category);

    @Query(value = "SELECT DISTINCT a.id FROM Announcement p JOIN p.attachments a")
    List<String> findAllAttachmentIdsUsed();
}
