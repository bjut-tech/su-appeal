package tech.bjut.su.appeal.controller;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.bjut.su.appeal.dto.AnnouncementCarouselCreateDto;
import tech.bjut.su.appeal.entity.AnnouncementCarousel;
import tech.bjut.su.appeal.jsonview.UserViews;
import tech.bjut.su.appeal.service.AnnouncementService;

import java.util.List;

@RestController
@RequestMapping("/announcements/carousels")
public class AnnouncementCarouselController {

    private final AnnouncementService announcementService;

    public AnnouncementCarouselController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping("")
    public List<AnnouncementCarousel> index() {
        return announcementService.getCarousels();
    }

    @GetMapping("/{id}")
    public AnnouncementCarousel show(@PathVariable("id") AnnouncementCarousel carousel) {
        return carousel;
    }

    @PostMapping("")
    @PreAuthorize("hasAuthority('ADMIN')")
    @JsonView(UserViews.Private.class)
    public AnnouncementCarousel store(@RequestBody AnnouncementCarouselCreateDto dto) {
        return announcementService.createCarousel(dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void destroy(@PathVariable("id") AnnouncementCarousel carousel) {
        announcementService.deleteCarousel(carousel);
    }
}
