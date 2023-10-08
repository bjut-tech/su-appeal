package tech.bjut.su.appeal.dto;

import lombok.Data;
import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.lang.Nullable;

import java.util.List;

@Data
public class CursorPaginationDto<T> {

    private List<T> data;

    @Nullable private String cursor;

    public CursorPaginationDto() {}

    public CursorPaginationDto(Window<T> pagination) {
        this.data = pagination.getContent();
        this.setCursorFrom(pagination);
    }

    public void setCursorFrom(Window<?> pagination) {
        if (!pagination.isEmpty()) {
            KeysetScrollPosition position = (KeysetScrollPosition) pagination.positionAt(pagination.getContent().size() - 1);
            this.cursor = position.getKeys().get("id").toString();
        }
    }
}
