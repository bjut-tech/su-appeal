package tech.bjut.su.appeal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.bjut.su.appeal.entity.Attachment;

import java.util.UUID;

public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {
}
