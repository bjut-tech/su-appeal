package tech.bjut.su.appeal.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.bjut.su.appeal.entity.QuestionCategory;
import tech.bjut.su.appeal.service.QuestionService;

import java.util.List;

@RestController
@RequestMapping("/questions/categories")
public class QuestionCategoryController {

    private final QuestionService questionService;

    public QuestionCategoryController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping("")
    public List<QuestionCategory> index() {
        return questionService.getCategories();
    }

    @GetMapping("/{id}")
    public QuestionCategory show(@PathVariable("id") QuestionCategory category) {
        return category;
    }
}
