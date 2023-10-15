package tech.bjut.su.appeal.controller;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.Valid;
import org.springframework.data.domain.Window;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tech.bjut.su.appeal.dto.CursorPaginationDto;
import tech.bjut.su.appeal.dto.QuestionCreateDto;
import tech.bjut.su.appeal.entity.Question;
import tech.bjut.su.appeal.entity.User;
import tech.bjut.su.appeal.jsonview.UserViews;
import tech.bjut.su.appeal.security.UserPrincipal;
import tech.bjut.su.appeal.service.QuestionService;
import tech.bjut.su.appeal.service.SecurityService;
import tech.bjut.su.appeal.service.UserService;

import java.util.Optional;

@RestController
@RequestMapping("/questions")
public class QuestionController {

    private final QuestionService service;

    private final UserService userService;

    private final SecurityService securityService;

    public QuestionController(
        QuestionService service,
        UserService userService,
        SecurityService securityService
    ) {
        this.service = service;
        this.userService = userService;
        this.securityService = securityService;
    }

    @GetMapping("")
    public MappingJacksonValue index(
        @RequestParam(required = false) String cursor
    ) {
        final boolean isAdmin = securityService.hasAuthority("ADMIN");

        Window<Question> pagination;
        if (isAdmin) {
            pagination = service.getPaginated(cursor);
        } else {
            pagination = service.getPublishedPaginated(cursor);
        }

        CursorPaginationDto<Question> dto = new CursorPaginationDto<>(pagination);
        MappingJacksonValue value = new MappingJacksonValue(dto);

        if (isAdmin) {
            value.setSerializationView(UserViews.Admin.class);
        } else {
            value.setSerializationView(UserViews.Public.class);
        }

        return value;
    }

    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    @JsonView(UserViews.Private.class)
    public CursorPaginationDto<Question> history(
        @RequestParam(required = false) String cursor
    ) {
        Window<Question> pagination = service.getPaginated(securityService.user(), cursor);

        return new CursorPaginationDto<>(pagination);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public MappingJacksonValue show(@PathVariable Long id) {
        final boolean isAdmin = securityService.hasAuthority("ADMIN");

        Optional<Question> question;
        if (isAdmin) {
            question = service.find(id);
        } else {
            question = service.find(securityService.user(), id);
        }

        if (question.isPresent()) {
            MappingJacksonValue value = new MappingJacksonValue(question.get());

            if (isAdmin) {
                value.setSerializationView(UserViews.Admin.class);
            } else {
                value.setSerializationView(UserViews.Private.class);
            }

            return value;
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found");
    }

    @PostMapping("")
    @JsonView(UserViews.Private.class)
    public Question store(
        Authentication auth,
        @Valid @RequestBody QuestionCreateDto dto
    ) {
        User user;
        if (auth == null) {
            user = userService.findOrCreate(dto.getUid(), dto.getName());
        } else {
            user = ((UserPrincipal) auth.getPrincipal()).getUser();
        }

        return service.create(user, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public void delete(@PathVariable Long id) {
        final boolean isAdmin = securityService.hasAuthority("ADMIN");

        if (isAdmin) {
            service.delete(id);
        } else {
            service.delete(securityService.user(), id);
        }
    }
}
