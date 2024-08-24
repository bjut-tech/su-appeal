package tech.bjut.su.appeal.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tech.bjut.su.appeal.dto.AnnouncementCategoryCreateDto;
import tech.bjut.su.appeal.entity.AnnouncementCategory;
import tech.bjut.su.appeal.service.AnnouncementService;
import tech.bjut.su.appeal.util.I18nHelper;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/announcements/categories")
public class AnnouncementCategoryController {

    private final I18nHelper i18nHelper;

    private final AnnouncementService announcementService;

    public AnnouncementCategoryController(
        I18nHelper i18nHelper,
        AnnouncementService announcementService
    ) {
        this.i18nHelper = i18nHelper;
        this.announcementService = announcementService;
    }

    @GetMapping("")
    public List<AnnouncementCategory> index() {
        return announcementService.getCategories();
    }

    @GetMapping("/{id}")
    public AnnouncementCategory show(@PathVariable Long id) {
        try {
            return announcementService.getCategory(id);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, i18nHelper.get("announcement-category.not-found"));
        }
    }

    @PostMapping("")
    @PreAuthorize("hasAuthority('ADMIN')")
    public AnnouncementCategory store(@Valid @RequestBody AnnouncementCategoryCreateDto dto) {
        return announcementService.createCategory(dto);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public AnnouncementCategory update(@PathVariable Long id, @Valid @RequestBody AnnouncementCategoryCreateDto dto) {
        try {
            return announcementService.updateCategory(id, dto);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, i18nHelper.get("announcement-category.not-found"));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void destroy(@PathVariable Long id) {
        announcementService.deleteCategory(id);
    }
}
