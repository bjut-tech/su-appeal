package tech.bjut.su.appeal.dto;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Window;
import org.springframework.lang.Nullable;
import tech.bjut.su.appeal.jsonview.UserViews;
import tech.bjut.su.appeal.util.CursorPaginationHelper;

import java.util.List;

@Data
@NoArgsConstructor
@JsonView(UserViews.Public.class)
public class CursorPaginationDto<T> {

    private List<T> data;

    @Nullable private String cursor;

    public CursorPaginationDto(Window<T> pagination) {
        this(pagination, false);
    }

    public CursorPaginationDto(Window<T> pagination, boolean withPinned) {
        this.data = pagination.getContent();

        this.cursor = CursorPaginationHelper.cursorOf(pagination, withPinned);
    }
}
