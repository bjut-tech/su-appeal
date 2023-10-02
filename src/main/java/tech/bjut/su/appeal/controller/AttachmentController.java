package tech.bjut.su.appeal.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tech.bjut.su.appeal.entity.Attachment;
import tech.bjut.su.appeal.service.AttachmentService;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/attachments")
public class AttachmentController {

    private final AttachmentService service;

    public AttachmentController(AttachmentService service) {
        this.service = service;
    }

    @PostMapping("")
    public String upload(
        @RequestParam("file") MultipartFile file,
        @RequestParam("name") String name
    ) {
        if (name == null || name.isEmpty()) {
            name = file.getOriginalFilename();
        }

        Attachment attachment = service.create(file, name);
        return attachment.getId().toString();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> download(@PathVariable("id") UUID id) {
        Attachment attachment = service.get(id);

        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=\"" + attachment.getName() + "\"")
            .contentLength(attachment.getSize())
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic().immutable())
            .body(service.getResource(attachment));
    }
}
