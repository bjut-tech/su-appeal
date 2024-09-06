package tech.bjut.su.appeal.dto;

import lombok.Data;
import org.springframework.lang.Nullable;
import tech.bjut.su.appeal.entity.User;
import tech.bjut.su.appeal.enums.CampusEnum;
import tech.bjut.su.appeal.enums.QuestionStatusEnum;

import java.util.List;

@Data
public class QuestionIndexDto {

    /**
     * null: return no questions;
     * non-null: return questions with these ids.
     */
    @Nullable
    private List<Long> ids;

    /**
     * null: return any user's questions;
     * non-null: return that one user's questions.
     */
    @Nullable
    private User user;

    @Nullable
    private QuestionStatusEnum status;

    @Nullable
    private CampusEnum campus;

    @Nullable
    private String search;

    @Nullable
    private String cursor;
}
