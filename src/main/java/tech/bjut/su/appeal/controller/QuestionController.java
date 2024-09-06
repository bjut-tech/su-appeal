package tech.bjut.su.appeal.controller;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.Valid;
import org.springframework.data.domain.Window;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tech.bjut.su.appeal.dto.*;
import tech.bjut.su.appeal.entity.Answer;
import tech.bjut.su.appeal.entity.Question;
import tech.bjut.su.appeal.entity.User;
import tech.bjut.su.appeal.enums.CampusEnum;
import tech.bjut.su.appeal.enums.QuestionStatusEnum;
import tech.bjut.su.appeal.jsonview.UserViews;
import tech.bjut.su.appeal.security.JwtResourceAuthoritiesHelper;
import tech.bjut.su.appeal.security.ResourceAuthority;
import tech.bjut.su.appeal.service.QuestionService;
import tech.bjut.su.appeal.service.SecurityService;
import tech.bjut.su.appeal.service.UserService;
import tech.bjut.su.appeal.util.I18nHelper;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/questions")
public class QuestionController {

    private final I18nHelper i18nHelper;

    private final JwtEncoder jwtEncoder;

    private final QuestionService service;

    private final UserService userService;

    private final SecurityService securityService;

    public QuestionController(
        I18nHelper i18nHelper,
        JwtEncoder jwtEncoder,
        QuestionService service,
        UserService userService,
        SecurityService securityService
    ) {
        this.i18nHelper = i18nHelper;
        this.jwtEncoder = jwtEncoder;
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
    @JsonView(UserViews.Private.class)
    public CursorPaginationDto<Question> history(
        @RequestParam(required = false) String cursor
    ) {
        User user = securityService.user();
        List<Long> ids = new ArrayList<>();
        if (securityService.authentication() != null) {
            for (GrantedAuthority authority : securityService.authentication().getAuthorities()) {
                if (authority instanceof ResourceAuthority resourceAuthority
                    && resourceAuthority.getEntityName().equals(ResourceAuthority.ENTITY_NAME_QUESTION)) {
                    ids.add(Long.valueOf(resourceAuthority.getEntityId()));
                }
            }
        }

        QuestionIndexDto dto = new QuestionIndexDto();
        dto.setIds(ids);
        dto.setUser(user);
        dto.setCursor(cursor);

        Window<Question> pagination = service.index(dto);

        return new CursorPaginationDto<>(pagination);
    }

    @GetMapping("/{id}")
    public MappingJacksonValue show(@PathVariable("id") Question question) {
        final boolean isAdmin = securityService.hasAuthority("ADMIN");
        final boolean isOwner = service.isOwner(question);

        if (!isAdmin && !question.isPublished() && !isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, i18nHelper.get("question.forbidden"));
        }

        MappingJacksonValue value = new MappingJacksonValue(question);
        if (isAdmin) {
            value.setSerializationView(UserViews.Admin.class);
        } else if (isOwner) {
            value.setSerializationView(UserViews.Private.class);
        } else {
            value.setSerializationView(UserViews.Public.class);
        }
        return value;
    }

    @PostMapping("")
    @JsonView(UserViews.Private.class)
    public ResourceTokenResponseDto<Question> store(@Valid @RequestBody QuestionCreateDto request) {
        User user = securityService.user();
        if (user == null && !request.getUid().isBlank()) {
            user = userService.findOrCreate(request.getUid(), request.getName());
        }

        Question question = service.create(user, request);

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        Instant now = Instant.now();
        long expiry = 2592000; // one month
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuedAt(now)
            .expiresAt(now.plusSeconds(expiry))
            .claim(
                JwtResourceAuthoritiesHelper.CLAIM_RESOURCES,
                JwtResourceAuthoritiesHelper.extractClaim(
                    securityService.authentication(),
                    new ResourceAuthority(ResourceAuthority.ENTITY_NAME_QUESTION, String.valueOf(question.getId()))
                )
            )
            .build();

        Jwt jwt = jwtEncoder.encode(JwtEncoderParameters.from(header, claims));

        TokenResponseDto tokenDto = new TokenResponseDto();
        tokenDto.setAccessToken(jwt.getTokenValue());
        tokenDto.setExpiresIn(expiry);

        ResourceTokenResponseDto<Question> response = new ResourceTokenResponseDto<>();
        response.setResourceToken(tokenDto);
        response.setData(question);

        return response;
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
    public void delete(@PathVariable("id") Question question) {
        final boolean isAdmin = securityService.hasAuthority("ADMIN");
        final boolean isOwner = service.isOwner(question);

        if (isAdmin || (!question.isPublished() && isOwner)) {
            service.delete(question);
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
