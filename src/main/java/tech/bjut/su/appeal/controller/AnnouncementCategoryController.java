package tech.bjut.su.appeal.controller;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.bjut.su.appeal.dto.AnnouncementCategoryCreateDto;
import tech.bjut.su.appeal.entity.AnnouncementCategory;
import tech.bjut.su.appeal.service.AnnouncementService;

import java.util.List;

@RestController
@RequestMapping("/announcements/categories")
public class AnnouncementCategoryController {

    private final AnnouncementService announcementService;

    public AnnouncementCategoryController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping("")
    public List<AnnouncementCategory> index() {
        return announcementService.getCategories();
    }

    @GetMapping("/{id}")
    public AnnouncementCategory show(@PathVariable("id") AnnouncementCategory category) {
        return category;
    }

    @PostMapping("")
    @PreAuthorize("hasAuthority('ADMIN')")
    public AnnouncementCategory store(@Valid @RequestBody AnnouncementCategoryCreateDto dto) {
        return announcementService.createCategory(dto);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public AnnouncementCategory update(
        @PathVariable("id") AnnouncementCategory category,
        @Valid @RequestBody AnnouncementCategoryCreateDto dto
    ) {
        return announcementService.updateCategory(category, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void destroy(@PathVariable("id") AnnouncementCategory category) {
        announcementService.deleteCategory(category);
    }
}
