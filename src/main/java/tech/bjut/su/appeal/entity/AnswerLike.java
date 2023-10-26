package tech.bjut.su.appeal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@DiscriminatorValue("ANSWER")
public class AnswerLike extends Like {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "answer_id", nullable = true)
    private Answer answer;

    @Column(name = "answer_id", insertable = false, updatable = false, nullable = true)
    private Long answerId;
}
