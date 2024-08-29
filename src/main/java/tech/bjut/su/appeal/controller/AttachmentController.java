package tech.bjut.su.appeal.controller;

import org.apache.tika.Tika;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import tech.bjut.su.appeal.entity.Attachment;
import tech.bjut.su.appeal.service.AttachmentService;
import tech.bjut.su.appeal.service.SecurityService;
import tech.bjut.su.appeal.util.I18nHelper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/attachments")
public class AttachmentController {

    private final I18nHelper i18nHelper;

    private final AttachmentService service;

    private final SecurityService securityService;

    public AttachmentController(
        I18nHelper i18nHelper,
        AttachmentService service,
        SecurityService securityService
    ) {
        this.i18nHelper = i18nHelper;
        this.service = service;
        this.securityService = securityService;
    }

    @PostMapping("")
    public String upload(
        @RequestParam("file") MultipartFile file,
        @RequestParam("name") String name
    ) {
        if (!securityService.hasAuthority("ADMIN")) {
            if (file.getSize() > 1024 * 1024 * 10) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, i18nHelper.get("attachment.file-too-large"));
            }

            try {
                InputStream stream = file.getInputStream();
                Tika tika = new Tika();
                String type = tika.detect(stream, file.getOriginalFilename());

                if (!type.startsWith("image/")) {
                    throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, i18nHelper.get("attachment.invalid-file-type"));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (file.getSize() > 1024 * 1024 * 100) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, i18nHelper.get("attachment.file-too-large"));
        }

        if (name == null || name.isEmpty()) {
            name = file.getOriginalFilename();
        }

        Attachment attachment = service.create(file, name);
        return attachment.getId().toString();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> download(@PathVariable("id") Attachment attachment) {
        try {
            String filenameEncoded = URLEncoder.encode(attachment.getName(), StandardCharsets.UTF_8);

            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + filenameEncoded + "\"")
                .contentLength(attachment.getSize())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic().immutable())
                .body(service.getResource(attachment));
        } catch (FileNotFoundException | NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, i18nHelper.get("attachment.not-found"));
        }
    }

    @GetMapping("/{id}/thumbnail")
    public ResponseEntity<Resource> thumbnail(@PathVariable("id") Attachment attachment) {
        try {
            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic().immutable())
                .body(service.getThumbnail(attachment));
        } catch (IOException | NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, i18nHelper.get("attachment.not-found"));
        }
    }
}
