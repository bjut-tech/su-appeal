package tech.bjut.su.appeal.util;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.apache.commons.lang3.StringUtils;

public class SpecificationHelper {

    @SafeVarargs
    public static Predicate search(CriteriaBuilder builder, String search, Path<String>... fields) {
        search = StringUtils.stripToEmpty(search);
        if (search.isEmpty()) {
            return null;
        }

        search = search.toLowerCase();
        Expression<String> pattern = builder.concat(builder.concat(builder.literal("%"), search), builder.literal("%"));
        Predicate[] predicates = new Predicate[fields.length];
        for (int i = 0; i < fields.length; i++) {
            predicates[i] = builder.like(builder.lower(fields[i]), pattern);
        }
        return builder.or(predicates);
    }
}
