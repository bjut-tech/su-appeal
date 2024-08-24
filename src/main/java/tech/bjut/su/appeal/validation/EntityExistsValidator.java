package tech.bjut.su.appeal.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.context.ApplicationContext;
import org.springframework.data.repository.CrudRepository;

public class EntityExistsValidator implements ConstraintValidator<EntityExists, Long> {

    private final ApplicationContext applicationContext;

    private CrudRepository<?, Long> repository;

    public EntityExistsValidator(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void initialize(EntityExists constraintAnnotation) {
        repository = applicationContext.getBean(constraintAnnotation.repository());
    }

    @Override
    public boolean isValid(Long value, ConstraintValidatorContext context) {
        if (value == null) {
            // other validation rules should handle null
            return true;
        }

        return repository.existsById(value);
    }
}
