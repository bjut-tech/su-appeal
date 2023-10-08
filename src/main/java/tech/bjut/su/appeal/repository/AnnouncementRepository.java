package tech.bjut.su.appeal.repository;

import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.repository.JpaRepository;
import tech.bjut.su.appeal.entity.Announcement;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    Window<Announcement> findFirst10ByOrderByIdDesc(KeysetScrollPosition position);
}
