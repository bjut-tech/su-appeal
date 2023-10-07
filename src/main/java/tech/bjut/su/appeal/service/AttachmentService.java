package tech.bjut.su.appeal.service;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tech.bjut.su.appeal.config.AppProperties;
import tech.bjut.su.appeal.entity.Attachment;
import tech.bjut.su.appeal.repository.AttachmentRepository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class AttachmentService {

    private final Path storePath;

    private final AttachmentRepository repository;

    public AttachmentService(
        AppProperties properties,
        AttachmentRepository repository
    ) {
        this.repository = repository;
        this.storePath = Path.of(properties.getStore().getPath());

        if (!this.storePath.toFile().exists() && !this.storePath.toFile().mkdirs()) {
            throw new RuntimeException("Failed to create store directory");
        }
    }

    public Attachment get(UUID id) {
        return repository.findById(id).orElseThrow();
    }

    public Resource getResource(Attachment attachment) {
        String fileName = attachment.getId().toString() + ".bin";
        Path filePath = storePath.resolve(fileName);

        return new FileSystemResource(filePath);
    }

    @Transactional
    public Attachment create(MultipartFile file, String name) {
        Attachment attachment = new Attachment();
        attachment.setName(name);
        attachment.setSize(file.getSize());
        attachment = store(attachment);

        try {
            String fileName = attachment.getId().toString() + ".bin";
            Path filePath = storePath.resolve(fileName);
            file.transferTo(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return attachment;
    }

    public Attachment store(Attachment attachment) {
        return repository.saveAndFlush(attachment);
    }
}
