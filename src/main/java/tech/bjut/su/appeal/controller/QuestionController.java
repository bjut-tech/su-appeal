package tech.bjut.su.appeal.controller;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.Valid;
import org.springframework.data.domain.Window;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tech.bjut.su.appeal.dto.CursorPaginationDto;
import tech.bjut.su.appeal.dto.QuestionAnswerDto;
import tech.bjut.su.appeal.dto.QuestionCreateDto;
import tech.bjut.su.appeal.dto.QuestionIndexDto;
import tech.bjut.su.appeal.entity.Answer;
import tech.bjut.su.appeal.entity.Question;
import tech.bjut.su.appeal.entity.User;
import tech.bjut.su.appeal.enums.CampusEnum;
import tech.bjut.su.appeal.enums.QuestionStatusEnum;
import tech.bjut.su.appeal.jsonview.UserViews;
import tech.bjut.su.appeal.service.QuestionService;
import tech.bjut.su.appeal.service.SecurityService;
import tech.bjut.su.appeal.service.UserService;
import tech.bjut.su.appeal.util.I18nHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/questions")
public class QuestionController {

    private final I18nHelper i18nHelper;

    private final QuestionService service;

    private final UserService userService;

    private final SecurityService securityService;

    public QuestionController(
        I18nHelper i18nHelper,
        QuestionService service,
        UserService userService,
        SecurityService securityService
    ) {
        this.i18nHelper = i18nHelper;
        this.service = service;
        this.userService = userService;
        this.securityService = securityService;
    }

    @GetMapping("/count")
    public Map<String, Long> count() {
        Map<String, Long> result = new HashMap<>(Map.of());

        User user = securityService.user();
        if (user != null) {
            result.put("history", service.countHistory(securityService.user()));
        }

        if (securityService.hasAuthority("ADMIN")) {
            result.put("unreplied", service.countUnreplied());
        }

        return result;
    }

    @GetMapping("")
    public MappingJacksonValue index(
        @RequestParam(required = false) QuestionStatusEnum status,
        @RequestParam(required = false) CampusEnum campus,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String cursor
    ) {
        final boolean isAdmin = securityService.hasAuthority("ADMIN");
        QuestionIndexDto request = new QuestionIndexDto();
        request.setStatus(isAdmin ? status : QuestionStatusEnum.PUBLISHED);
        request.setCampus(campus);
        request.setSearch(search);
        request.setCursor(cursor);

        Window<Question> pagination = service.index(request);
        CursorPaginationDto<Question> response = new CursorPaginationDto<>(pagination);
        MappingJacksonValue value = new MappingJacksonValue(response);
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
        QuestionIndexDto dto = new QuestionIndexDto();
        dto.setUser(securityService.user());
        dto.setCursor(cursor);

        Window<Question> pagination = service.index(dto);

        return new CursorPaginationDto<>(pagination);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public MappingJacksonValue show(@PathVariable("id") Question question) {
        final boolean isAdmin = securityService.hasAuthority("ADMIN");
        final boolean isOwn = question.getUser().equals(securityService.user());

        if (!isAdmin && !question.isPublished() && !isOwn) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, i18nHelper.get("question.not-found"));
        }

        MappingJacksonValue value = new MappingJacksonValue(question);
        if (isAdmin) {
            value.setSerializationView(UserViews.Admin.class);
        } else if (isOwn) {
            value.setSerializationView(UserViews.Private.class);
        } else {
            value.setSerializationView(UserViews.Public.class);
        }
        return value;
    }

    @PostMapping("")
    @JsonView(UserViews.Private.class)
    public Question store(@Valid @RequestBody QuestionCreateDto dto) {
        User user = securityService.user();
        if (user == null && !dto.getUid().isBlank()) {
            user = userService.findOrCreate(dto.getUid(), dto.getName());
        }

        return service.create(user, dto);
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void publish(@PathVariable("id") Question question) {
        service.setPublished(question, true);
    }

    @DeleteMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void unpublish(@PathVariable("id") Question question) {
        service.setPublished(question, false);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public void delete(@PathVariable("id") Question question) {
        final boolean isAdmin = securityService.hasAuthority("ADMIN");

        if (isAdmin) {
            service.delete(question);
        } else {
            service.delete(securityService.user(), question);
        }
    }

    @PostMapping("/{id}/answer")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Question answer(
        @PathVariable("id") Question question,
        @Valid @RequestBody QuestionAnswerDto dto
    ) {
        return service.answer(question, securityService.user(), dto);
    }

    @DeleteMapping("/{id}/answer")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteAnswer(@PathVariable("id") Question question) {
        service.deleteAnswer(question);
    }

    @GetMapping("/liked-answers")
    @PreAuthorize("isAuthenticated()")
    public Set<Long> getLikedAnswers() {
        return service.getLikedAnswerIds(securityService.user());
    }

    @PostMapping("/{id}/answer/like")
    public void likeAnswer(@PathVariable("id") Question question) {
        if (!question.isPublished()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, i18nHelper.get("question.not-found"));
        }

        Answer answer = question.getAnswer();
        service.likeAnswer(securityService.user(), answer);
    }

    @DeleteMapping("/{id}/answer/like")
    @PreAuthorize("isAuthenticated()")
    public void unlikeAnswer(@PathVariable("id") Question question) {
        Answer answer = question.getAnswer();
        service.unlikeAnswer(securityService.user(), answer);
    }
}
