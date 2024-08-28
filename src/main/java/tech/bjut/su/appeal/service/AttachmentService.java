package tech.bjut.su.appeal.service;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tech.bjut.su.appeal.config.AppProperties;
import tech.bjut.su.appeal.entity.Attachment;
import tech.bjut.su.appeal.repository.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
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

    public File getFile(Attachment attachment) throws FileNotFoundException {
        String fileName = attachment.getId().toString() + ".bin";
        Path filePath = storePath.resolve(fileName);

        if (!filePath.toFile().exists()) {
            throw new FileNotFoundException();
        }

        return filePath.toFile();
    }

    public Resource getResource(Attachment attachment) throws FileNotFoundException {
        return new FileSystemResource(getFile(attachment));
    }

    public Resource getThumbnail(Attachment attachment) throws IOException {
        BufferedImage image = ImageIO.read(getFile(attachment));
        if (image == null) {
            throw new IOException("Failed to read attachment as image");
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Thumbnails.Builder<BufferedImage> builder = Thumbnails.of(image);
        if (image.getWidth() > 400 || image.getHeight() > 400) {
            builder.size(400, 400)
                .crop(Positions.CENTER);
        } else {
            builder.scale(1);
        }
        builder.outputQuality(0.8)
            .outputFormat("jpg")
            .toOutputStream(os);

        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        return new InputStreamResource(is);
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
        return repository.save(attachment);
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

        AnnouncementCarouselRepository repository2 = context.getBean(AnnouncementCarouselRepository.class);
        filesUsed.addAll(repository2.findAllAttachmentIdsUsed());

        QuestionRepository repository3 = context.getBean(QuestionRepository.class);
        filesUsed.addAll(repository3.findAllAttachmentIdsUsed());

        AnswerRepository repository4 = context.getBean(AnswerRepository.class);
        filesUsed.addAll(repository4.findAllAttachmentIdsUsed());

        logger.trace("Files used: {}", filesUsed);

        List<String> filesToRemove = new ArrayList<>();
        long timeThreshold = System.currentTimeMillis() - 24 * 60 * 60 * 1000;
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
