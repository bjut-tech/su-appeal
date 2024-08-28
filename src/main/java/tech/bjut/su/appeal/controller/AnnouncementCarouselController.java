package tech.bjut.su.appeal.controller;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tech.bjut.su.appeal.dto.AnnouncementCarouselCreateDto;
import tech.bjut.su.appeal.entity.AnnouncementCarousel;
import tech.bjut.su.appeal.jsonview.UserViews;
import tech.bjut.su.appeal.service.AnnouncementService;
import tech.bjut.su.appeal.util.I18nHelper;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/announcements/carousels")
public class AnnouncementCarouselController {

    private final I18nHelper i18nHelper;

    private final AnnouncementService announcementService;

    public AnnouncementCarouselController(
        I18nHelper i18nHelper,
        AnnouncementService announcementService
    ) {
        this.i18nHelper = i18nHelper;
        this.announcementService = announcementService;
    }

    @GetMapping("")
    public List<AnnouncementCarousel> index() {
        return announcementService.getCarousels();
    }

    @GetMapping("/{id}")
    public AnnouncementCarousel show(@PathVariable Long id) {
        try {
            return announcementService.getCarousel(id);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, i18nHelper.get("announcement.not-found"));
        }
    }

    @PostMapping("")
    @PreAuthorize("hasAuthority('ADMIN')")
    @JsonView(UserViews.Private.class)
    public AnnouncementCarousel store(@RequestBody AnnouncementCarouselCreateDto dto) {
        return announcementService.createCarousel(dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void destroy(@PathVariable Long id) {
        try {
            announcementService.deleteCarousel(id);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, i18nHelper.get("announcement.not-found"));
        }
    }
}
