package tech.bjut.su.appeal.controller;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.Valid;
import org.springframework.data.domain.Window;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.bjut.su.appeal.dto.AnnouncementCreateDto;
import tech.bjut.su.appeal.dto.AnnouncementIndexDto;
import tech.bjut.su.appeal.dto.CursorPaginationDto;
import tech.bjut.su.appeal.entity.Announcement;
import tech.bjut.su.appeal.jsonview.UserViews;
import tech.bjut.su.appeal.service.AnnouncementService;
import tech.bjut.su.appeal.service.SecurityService;

@RestController
@RequestMapping("/announcements")
public class AnnouncementController {

    private final AnnouncementService service;

    private final SecurityService securityService;

    public AnnouncementController(
        AnnouncementService service,
        SecurityService securityService
    ) {
        this.service = service;
        this.securityService = securityService;
    }

    @GetMapping("")
    public MappingJacksonValue index(
        @RequestParam(name = "category", required = false) Long categoryId,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String cursor
    ) {
        AnnouncementIndexDto request = new AnnouncementIndexDto();
        request.setCategoryId(categoryId);
        request.setSearch(search);
        request.setCursor(cursor);

        Window<Announcement> pagination = service.index(request);
        CursorPaginationDto<Announcement> response = new CursorPaginationDto<>(pagination, true);
        MappingJacksonValue value = new MappingJacksonValue(response);
        if (securityService.hasAuthority("ADMIN")) {
            value.setSerializationView(UserViews.Admin.class);
        } else {
            value.setSerializationView(UserViews.Public.class);
        }

        return value;
    }

    @GetMapping("/{id}")
    public MappingJacksonValue show(@PathVariable("id") Announcement announcement) {
        MappingJacksonValue value = new MappingJacksonValue(announcement);
        if (securityService.hasAuthority("ADMIN")) {
            value.setSerializationView(UserViews.Admin.class);
        } else {
            value.setSerializationView(UserViews.Public.class);
        }

        return value;
    }

    @PostMapping("")
    @PreAuthorize("hasAuthority('ADMIN')")
    @JsonView(UserViews.Private.class)
    public Announcement store(@Valid @RequestBody AnnouncementCreateDto dto) {
        return service.create(securityService.user(), dto);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @JsonView(UserViews.Private.class)
    public Announcement update(@PathVariable("id") Announcement announcement, @Valid @RequestBody AnnouncementCreateDto dto) {
        return service.update(announcement, securityService.user(), dto);
    }

    @PostMapping("/{id}/pin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void pin(@PathVariable("id") Announcement announcement) {
        service.setPinned(announcement, true);
    }

    @DeleteMapping("/{id}/pin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void unpin(@PathVariable("id") Announcement announcement) {
        service.setPinned(announcement, false);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void destroy(@PathVariable("id") Announcement announcement) {
        service.delete(announcement);
    }
}
