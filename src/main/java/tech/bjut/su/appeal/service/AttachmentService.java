package tech.bjut.su.appeal.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tech.bjut.su.appeal.config.AppProperties;
import tech.bjut.su.appeal.entity.Attachment;
import tech.bjut.su.appeal.repository.AnnouncementRepository;
import tech.bjut.su.appeal.repository.AnswerRepository;
import tech.bjut.su.appeal.repository.AttachmentRepository;
import tech.bjut.su.appeal.repository.QuestionRepository;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@Service
public class AttachmentService {

    private final ApplicationContext context;

    private final AttachmentRepository repository;

    private final Path storePath;

    private static final Logger logger = LoggerFactory.getLogger(AttachmentService.class);

    public AttachmentService(
        ApplicationContext context,
        AppProperties properties,
        AttachmentRepository attachmentRepository
    ) {
        this.context = context;
        this.repository = attachmentRepository;
        this.storePath = Path.of(properties.getStore().getPath());

        if (!this.storePath.toFile().exists() && !this.storePath.toFile().mkdirs()) {
            throw new RuntimeException("Failed to create store directory");
        }
    }

    public Attachment get(UUID id) {
        return repository.findById(id).orElseThrow();
    }

    public Resource getResource(Attachment attachment) throws FileNotFoundException {
        String fileName = attachment.getId().toString() + ".bin";
        Path filePath = storePath.resolve(fileName);

        if (!filePath.toFile().exists()) {
            throw new FileNotFoundException();
        }

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

    /**
     * Prune attachments that are uploaded more than 7 days ago and are not used.
     *
     * @return The number of attachments pruned.
     */
    @Transactional
    public long prune() {
        Set<String> filesUsed = new HashSet<>();

        AnnouncementRepository repository1 = context.getBean(AnnouncementRepository.class);
        filesUsed.addAll(repository1.findAllAttachmentIdsUsed());

        QuestionRepository repository2 = context.getBean(QuestionRepository.class);
        filesUsed.addAll(repository2.findAllAttachmentIdsUsed());

        AnswerRepository repository3 = context.getBean(AnswerRepository.class);
        filesUsed.addAll(repository3.findAllAttachmentIdsUsed());

        logger.trace("Files used: {}", filesUsed);

        List<String> filesToRemove = new ArrayList<>();
        long timeThreshold = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000;
        for (String fileName : Objects.requireNonNull(storePath.toFile().list())) {
            File file = storePath.resolve(fileName).toFile();

            if (!file.isFile() || file.lastModified() > timeThreshold) {
                continue;
            }

            String id = fileName.substring(0, fileName.length() - 4);
            if (!filesUsed.contains(id)) {
                filesToRemove.add(id);
            }
        }

        logger.trace("Files to remove: {}", filesToRemove);

        long count = 0;
        for (String id : filesToRemove) {
            File file = storePath.resolve(id + ".bin").toFile();

            if (!file.delete()) {
                logger.warn("Failed to delete file: {}", id);
                continue;
            }

            repository.deleteById(UUID.fromString(id));
            count++;
        }

        return count;
    }
}
