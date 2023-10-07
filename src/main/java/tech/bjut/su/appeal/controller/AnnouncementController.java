package tech.bjut.su.appeal.controller;

import jakarta.validation.Valid;
import org.springframework.core.convert.ConversionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.bjut.su.appeal.dto.AnnouncementCreateDto;
import tech.bjut.su.appeal.dto.AnnouncementShowDto;
import tech.bjut.su.appeal.entity.Announcement;
import tech.bjut.su.appeal.security.UserPrincipal;
import tech.bjut.su.appeal.service.AnnouncementService;

@RestController
@RequestMapping("/announcements")
public class AnnouncementController {

    private final AnnouncementService service;

    private final ConversionService conversionService;

    public AnnouncementController(
        AnnouncementService service,
        ConversionService conversionService
    ) {
        this.service = service;
        this.conversionService = conversionService;
    }

    @PostMapping("")
    @PreAuthorize("hasAuthority('ADMIN')")
    public AnnouncementShowDto store(
        Authentication auth,
        @Valid @RequestBody AnnouncementCreateDto dto
    ) {
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        Announcement announcement = service.create(principal.getUser(), dto);

        return conversionService.convert(announcement, AnnouncementShowDto.class);
    }
}
