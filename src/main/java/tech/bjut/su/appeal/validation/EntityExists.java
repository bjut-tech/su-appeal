package tech.bjut.su.appeal.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.springframework.data.repository.CrudRepository;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EntityExistsValidator.class)
public @interface EntityExists {

    String message() default "{entity.not-found}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    Class<? extends CrudRepository<?, Long>> repository();
}
