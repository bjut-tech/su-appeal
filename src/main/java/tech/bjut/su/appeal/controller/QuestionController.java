package tech.bjut.su.appeal.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.bjut.su.appeal.dto.QuestionCreateDTO;
import tech.bjut.su.appeal.entity.Question;
import tech.bjut.su.appeal.service.QuestionService;

@RestController
@RequestMapping("/questions")
public class QuestionController {

    private final QuestionService service;

    public QuestionController(QuestionService service) {
        this.service = service;
    }

    @PostMapping("")
    public Question store(@Valid @RequestBody QuestionCreateDTO dto) {
        return service.create(dto);
    }
}
