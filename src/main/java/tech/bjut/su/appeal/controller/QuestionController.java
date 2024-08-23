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
import tech.bjut.su.appeal.entity.Answer;
import tech.bjut.su.appeal.entity.Question;
import tech.bjut.su.appeal.entity.User;
import tech.bjut.su.appeal.jsonview.UserViews;
import tech.bjut.su.appeal.service.QuestionService;
import tech.bjut.su.appeal.service.SecurityService;
import tech.bjut.su.appeal.service.UserService;
import tech.bjut.su.appeal.util.I18nHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    public MappingJacksonValue index(@RequestParam(required = false) String cursor) {
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

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, i18nHelper.get("question.not-found"));
    }

    @PostMapping("")
    @JsonView(UserViews.Private.class)
    public Question store(@Valid @RequestBody QuestionCreateDto dto) {
        User user = securityService.user();
        if (user == null) {
            if (dto.getUid().isBlank()) {
                dto.setUid("anonymous");
                dto.setName(i18nHelper.get("user.anonymous-name"));
            }
            user = userService.findOrCreate(dto.getUid(), dto.getName());
        }

        return service.create(user, dto);
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void publish(@PathVariable Long id) {
        Question question = service.find(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, i18nHelper.get("question.not-found"))
        );

        service.setPublished(question, true);
    }

    @DeleteMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void unpublish(@PathVariable Long id) {
        Question question = service.find(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, i18nHelper.get("question.not-found"))
        );

        service.setPublished(question, false);
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

    @PostMapping("/{id}/answer")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Question answer(
        @PathVariable Long id,
        @Valid @RequestBody QuestionAnswerDto dto
    ) {
        Question question = service.find(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, i18nHelper.get("question.not-found"))
        );

        return service.answer(question, securityService.user(), dto);
    }

    @DeleteMapping("/{id}/answer")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteAnswer(@PathVariable Long id) {
        Question question = service.find(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, i18nHelper.get("question.not-found"))
        );

        service.deleteAnswer(question);
    }

    @GetMapping("/liked-answers")
    @PreAuthorize("isAuthenticated()")
    public List<Long> getLikedAnswers() {
        return service.getLikedAnswerIds(securityService.user());
    }

    @PostMapping("/{id}/answer/like")
    public void likeAnswer(@PathVariable Long id) {
        Answer answer = service.findAnswerOf(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, i18nHelper.get("answer.not-found"))
        );

        service.likeAnswer(securityService.user(), answer);
    }

    @DeleteMapping("/{id}/answer/like")
    @PreAuthorize("isAuthenticated()")
    public void unlikeAnswer(@PathVariable Long id) {
        Answer answer = service.findAnswerOf(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, i18nHelper.get("answer.not-found"))
        );

        service.unlikeAnswer(securityService.user(), answer);
    }
}
