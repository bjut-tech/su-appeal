package tech.bjut.su.appeal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.bjut.su.appeal.entity.AnnouncementCategory;

public interface AnnouncementCategoryRepository extends JpaRepository<AnnouncementCategory, Long> {
}
