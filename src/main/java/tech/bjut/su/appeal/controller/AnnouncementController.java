package tech.bjut.su.appeal.controller;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.Valid;
import org.springframework.data.domain.Window;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
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
    @JsonView(UserViews.Public.class)
    public CursorPaginationDto<Announcement> index(
        @RequestParam(required = false) String cursor
    ) {
        Window<Announcement> pagination = service.getPaginated(cursor);

        return new CursorPaginationDto<>(pagination);
    }

    @PostMapping("")
    @PreAuthorize("hasAuthority('ADMIN')")
    @JsonView(UserViews.Private.class)
    public Announcement store(@Valid @RequestBody AnnouncementCreateDto dto) {
        return service.create(securityService.user(), dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void destroy(@PathVariable Long id) {
        service.delete(id);
    }
}
