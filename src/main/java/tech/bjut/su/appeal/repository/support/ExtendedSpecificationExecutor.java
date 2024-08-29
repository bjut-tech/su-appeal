package tech.bjut.su.appeal.repository.support;

import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ExtendedSpecificationExecutor<T> extends JpaSpecificationExecutor<T> {

    default Window<T> findAllPaginatedOrderByIdDesc(Specification<T> spec, int pageSize, KeysetScrollPosition position) {
        return findBy(spec, query -> query
            .limit(pageSize)
            .sortBy(Sort.by("id").descending())
            .scroll(position));
    }

    default Window<T> findAllPaginatedOrderByPinnedDescAndIdDesc(Specification<T> spec, int pageSize, KeysetScrollPosition position) {
        return findBy(spec, query -> query
            .limit(pageSize)
            .sortBy(Sort.by("pinned", "id").descending())
            .scroll(position));
    }
}
