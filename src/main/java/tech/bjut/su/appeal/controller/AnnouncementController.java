package tech.bjut.su.appeal.controller;

import jakarta.validation.Valid;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.data.domain.Window;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tech.bjut.su.appeal.dto.AnnouncementCreateDto;
import tech.bjut.su.appeal.dto.AnnouncementShowDto;
import tech.bjut.su.appeal.dto.CursorPaginationDto;
import tech.bjut.su.appeal.entity.Announcement;
import tech.bjut.su.appeal.security.UserPrincipal;
import tech.bjut.su.appeal.service.AnnouncementService;

import java.util.List;

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

    @GetMapping("")
    public CursorPaginationDto<AnnouncementShowDto> index(
        @RequestParam(required = false) String cursor
    ) {
        Window<Announcement> pagination = service.getPaginated(cursor);

        CursorPaginationDto<AnnouncementShowDto> dto = new CursorPaginationDto<>();

        TypeDescriptor sourceType = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(Announcement.class));
        TypeDescriptor targetType = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(AnnouncementShowDto.class));
        //noinspection unchecked
        dto.setData((List<AnnouncementShowDto>) conversionService.convert(pagination.getContent(), sourceType, targetType));
        dto.setCursorFrom(pagination);

        return dto;
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
