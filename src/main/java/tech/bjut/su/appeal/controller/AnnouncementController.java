package tech.bjut.su.appeal.controller;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.Valid;
import org.springframework.data.domain.Window;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tech.bjut.su.appeal.dto.AnnouncementCreateDto;
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
        @RequestParam(required = false) String cursor
    ) {
        Window<Announcement> pagination = service.getPaginated(cursor);

        CursorPaginationDto<Announcement> dto = new CursorPaginationDto<>(pagination);
        if (cursor == null || cursor.isBlank()) {
            dto.setPinned(service.getPinned());
        }

        MappingJacksonValue value = new MappingJacksonValue(dto);
        if (securityService.hasAuthority("ADMIN")){
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

    @PostMapping("/{id}/pin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void pin(@PathVariable Long id) {
        Announcement announcement = service.find(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found")
        );

        service.setPinned(announcement, true);
    }

    @DeleteMapping("/{id}/pin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void unpin(@PathVariable Long id) {
        Announcement announcement = service.find(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found")
        );

        service.setPinned(announcement, false);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void destroy(@PathVariable Long id) {
        service.delete(id);
    }
}
