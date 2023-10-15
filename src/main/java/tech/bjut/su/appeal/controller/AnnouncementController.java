package tech.bjut.su.appeal.controller;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.Valid;
import org.springframework.data.domain.Window;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tech.bjut.su.appeal.dto.AnnouncementCreateDto;
import tech.bjut.su.appeal.dto.CursorPaginationDto;
import tech.bjut.su.appeal.entity.Announcement;
import tech.bjut.su.appeal.jsonview.UserViews;
import tech.bjut.su.appeal.security.UserPrincipal;
import tech.bjut.su.appeal.service.AnnouncementService;

@RestController
@RequestMapping("/announcements")
public class AnnouncementController {

    private final AnnouncementService service;

    public AnnouncementController(AnnouncementService service) {
        this.service = service;
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
    public Announcement store(
        Authentication auth,
        @Valid @RequestBody AnnouncementCreateDto dto
    ) {
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();

        return service.create(principal.getUser(), dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void destroy(@PathVariable Long id) {
        service.delete(id);
    }
}
