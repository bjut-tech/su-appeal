package tech.bjut.su.appeal.service;

import jakarta.persistence.criteria.Expression;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.bjut.su.appeal.dto.QuestionAnswerDto;
import tech.bjut.su.appeal.dto.QuestionCreateDto;
import tech.bjut.su.appeal.dto.QuestionIndexDto;
import tech.bjut.su.appeal.entity.*;
import tech.bjut.su.appeal.repository.AnswerLikeRepository;
import tech.bjut.su.appeal.repository.AnswerRepository;
import tech.bjut.su.appeal.repository.AttachmentRepository;
import tech.bjut.su.appeal.repository.QuestionRepository;
import tech.bjut.su.appeal.util.CursorPaginationHelper;

import java.util.List;

@Service
public class QuestionService {
    private final QuestionRepository repository;

    private final AnswerRepository answerRepository;

    private final AnswerLikeRepository likeRepository;

    private final AttachmentRepository attachmentRepository;

    public QuestionService(
        QuestionRepository repository,
        AnswerRepository answerRepository,
        AnswerLikeRepository likeRepository,
        AttachmentRepository attachmentRepository
    ) {
        this.repository = repository;
        this.answerRepository = answerRepository;
        this.likeRepository = likeRepository;
        this.attachmentRepository = attachmentRepository;
    }

    public Window<Question> index(QuestionIndexDto dto) {
        KeysetScrollPosition position = CursorPaginationHelper.positionOf(dto.getCursor());
        Specification<Question> spec = Specification.allOf((root, query, builder) -> {
            if (dto.getUser() == null) {
                return null;
            }
            return builder.equal(root.get("user"), dto.getUser());
        }, (root, query, builder) -> {
            if (dto.getStatus() == null) {
                return null;
            }
            return switch (dto.getStatus()) {
                case NOT_REPLIED -> builder.isNull(root.get("answer"));
                case NOT_PUBLISHED -> builder.and(
                    builder.isNotNull(root.get("answer")),
                    builder.isFalse(root.get("published"))
                );
                case PUBLISHED -> builder.isTrue(root.get("published"));
            };
        }, (root, query, builder) -> {
            if (dto.getCampus() == null) {
                return null;
            }
            return builder.equal(root.get("campus"), dto.getCampus());
        }, (root, query, builder) -> {
            String search = StringUtils.trimToEmpty(dto.getSearch());
            if (search.isEmpty()) {
                return null;
            }
            Expression<String> pattern = builder.concat(builder.concat(builder.literal("%"), search), builder.literal("%"));
            return builder.or(
                builder.like(root.get("content"), pattern),
                builder.like(root.get("answer").get("content"), pattern)
            );
        });

        return repository.findAllPaginatedOrderByIdDesc(spec, 10, position);
    }

    public long countHistory(User user) {
        return repository.countByUser(user);
    }

    public long countUnreplied() {
        return repository.countByAnswerNull();
    }

    @Transactional
    public Question create(@Nullable User user, QuestionCreateDto dto) {
        Question question = new Question();
        question.setUser(user);
        question.setContact(StringUtils.stripToEmpty(dto.getContact()));
        question.setCampus(dto.getCampus());
        question.setContent(StringUtils.stripToEmpty(dto.getContent()));

        if (dto.getAttachmentIds() != null && !dto.getAttachmentIds().isEmpty()) {
            List<Attachment> existingAttachments = attachmentRepository.findAllById(dto.getAttachmentIds());
            question.setAttachments(existingAttachments);
        }

        return repository.save(question);
    }

    public void setPublished(Question question, boolean published) {
        question.setPublished(published);
        repository.save(question);
    }

    public void delete(Question question) {
        repository.delete(question);
    }

    public void delete(User user, Question question) {
        if (!question.isPublished() && question.getUser().equals(user)) {
            repository.delete(question);
        }
    }

    @Transactional
    public Question answer(Question question, User user, QuestionAnswerDto dto) {
        Answer answer;
        if (question.getAnswer() == null) {
            answer = new Answer();
            answer.setQuestion(question);
        } else {
            answer = question.getAnswer();
        }

        answer.setUser(user);
        answer.setContent(StringUtils.stripToEmpty(dto.getContent()));

        if (dto.getAttachmentIds() != null && !dto.getAttachmentIds().isEmpty()) {
            List<Attachment> existingAttachments = attachmentRepository.findAllById(dto.getAttachmentIds());
            answer.setAttachments(existingAttachments);
        }

        answer = answerRepository.save(answer);
        if (question.getAnswer() == null) {
            question.setAnswer(answer);
        }

        return repository.save(question);
    }

    @Transactional
    public void deleteAnswer(Question question) {
        Answer answer = question.getAnswer();
        if (answer != null) {
            question.setPublished(false);
            question.setAnswer(null);
            repository.save(question);
            answerRepository.delete(answer);
        }
    }

    @Transactional
    public void likeAnswer(User user, Answer answer) {
        if (user != null) {
            if (likeRepository.findByUserAndAnswer(user, answer).isPresent()) {
                return;
            }

            AnswerLike like = new AnswerLike();
            like.setUser(user);
            like.setAnswer(answer);
            likeRepository.save(like);
        }

        answer.setLikesCount(answer.getLikesCount() + 1);
        answerRepository.save(answer);
    }

    @Transactional
    public void unlikeAnswer(User user, Answer answer) {
        if (user != null) {
            likeRepository.findByUserAndAnswer(user, answer).ifPresent(likeRepository::delete);
        }

        answer.setLikesCount(Math.max(answer.getLikesCount() - 1, 0));
        answerRepository.save(answer);
    }

    public List<Long> getLikedAnswerIds(User user) {
        return likeRepository.findByUser(user).stream()
                .map(AnswerLike::getAnswerId)
                .toList();
    }
}
