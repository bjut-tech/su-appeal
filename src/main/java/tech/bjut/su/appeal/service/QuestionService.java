package tech.bjut.su.appeal.service;

import org.springframework.stereotype.Service;
import tech.bjut.su.appeal.dto.QuestionCreateDTO;
import tech.bjut.su.appeal.entity.Attachment;
import tech.bjut.su.appeal.entity.Question;
import tech.bjut.su.appeal.repository.AttachmentRepository;
import tech.bjut.su.appeal.repository.QuestionRepository;

import java.util.List;

@Service
public class QuestionService {
    private final QuestionRepository questionRepository;

    private final AttachmentRepository attachmentRepository;

    public QuestionService(
        QuestionRepository questionRepository,
        AttachmentRepository attachmentRepository
    ) {
        this.questionRepository = questionRepository;
        this.attachmentRepository = attachmentRepository;
    }

    public Question create(QuestionCreateDTO dto) {
        Question question = new Question();
        question.setContent(dto.getContent());

        if (dto.getAttachmentIds() != null && !dto.getAttachmentIds().isEmpty()) {
            List<Attachment> existingAttachments = attachmentRepository.findAllById(dto.getAttachmentIds());
            question.setAttachments(existingAttachments);
        }

        return this.store(question);
    }

    public Question store(Question question) {
        return questionRepository.saveAndFlush(question);
    }
}
