package tech.bjut.su.appeal.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.bjut.su.appeal.dto.QuestionCreateDto;
import tech.bjut.su.appeal.entity.Question;
import tech.bjut.su.appeal.entity.User;
import tech.bjut.su.appeal.security.UserPrincipal;
import tech.bjut.su.appeal.service.QuestionService;
import tech.bjut.su.appeal.service.UserService;

@RestController
@RequestMapping("/questions")
public class QuestionController {

    private final QuestionService service;

    private final UserService userService;

    public QuestionController(
        QuestionService service,
        UserService userService
    ) {
        this.service = service;
        this.userService = userService;
    }

    @PostMapping("")
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
}
