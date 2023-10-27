package tech.bjut.su.appeal.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tech.bjut.su.appeal.service.AttachmentService;

import java.util.concurrent.TimeUnit;

@Component
public class StoragePruneTask {

    private final AttachmentService service;

    private static final Logger logger = LoggerFactory.getLogger(StoragePruneTask.class);

    public StoragePruneTask(AttachmentService attachmentService) {
        this.service = attachmentService;
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.DAYS)
    public void work() {
        logger.info("Pruning storage...");

        long count = service.prune();

        logger.info("Pruned {} files", count);
    }
}
