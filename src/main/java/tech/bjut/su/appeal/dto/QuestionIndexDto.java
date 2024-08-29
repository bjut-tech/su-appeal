package tech.bjut.su.appeal.dto;

import lombok.Data;
import org.springframework.lang.Nullable;
import tech.bjut.su.appeal.entity.User;
import tech.bjut.su.appeal.enums.CampusEnum;
import tech.bjut.su.appeal.enums.QuestionStatusEnum;

@Data
public class QuestionIndexDto {

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
