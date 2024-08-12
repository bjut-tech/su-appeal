package tech.bjut.su.appeal.service;

import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.bjut.su.appeal.dto.QuestionAnswerDto;
import tech.bjut.su.appeal.dto.QuestionCreateDto;
import tech.bjut.su.appeal.entity.*;
import tech.bjut.su.appeal.repository.AnswerLikeRepository;
import tech.bjut.su.appeal.repository.AnswerRepository;
import tech.bjut.su.appeal.repository.AttachmentRepository;
import tech.bjut.su.appeal.repository.QuestionRepository;
import tech.bjut.su.appeal.util.CursorPagination;

import java.util.List;
import java.util.Optional;

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

    public Window<Question> getPaginated(@Nullable String cursor) {
        KeysetScrollPosition position = CursorPagination.positionOf(cursor);

        return repository.findFirst10ByOrderByIdDesc(position);
    }

    public Window<Question> getPaginated(User user, @Nullable String cursor) {
        KeysetScrollPosition position = CursorPagination.positionOf(cursor);

        return repository.findFirst10ByUserOrderByIdDesc(user, position);
    }

    public Window<Question> getPublishedPaginated(@Nullable String cursor) {
        KeysetScrollPosition position = CursorPagination.positionOf(cursor);

        return repository.findFirst10ByPublishedTrueOrderByIdDesc(position);
    }

    public Optional<Question> find(Long id) {
        return repository.findById(id);
    }

    public Optional<Question> find(User user, Long id) {
        return repository.findByIdAndUser(id, user);
    }

    public long countHistory(User user) {
        return repository.countByUser(user);
    }

    public long countUnreplied() {
        return repository.countByAnswerNull();
    }

    @Transactional
    public Question create(User user, QuestionCreateDto dto) {
        Question question = new Question();
        question.setUser(user);
        question.setContact(dto.getContact());
        question.setCampus(dto.getCampus());
        question.setContent(dto.getContent());

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

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public void delete(User user, Long id) {
        repository.deleteByPublishedFalseAndIdAndUser(id, user);
    }

    public Optional<Answer> findAnswerOf(Long id) {
        return answerRepository.findByQuestionId(id);
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
        answer.setContent(dto.getContent());

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
