package tech.bjut.su.appeal.util;

import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.lang.NonNull;

import java.util.Map;

public class CursorPagination {

    public static KeysetScrollPosition positionOf(String cursor) {
        return positionOf(cursor, "id");
    }

    public static KeysetScrollPosition positionOf(String cursor, String key) {
        if (cursor == null) {
            return ScrollPosition.keyset();
        } else {
            return ScrollPosition.of(Map.of(key, cursor), ScrollPosition.Direction.FORWARD);
        }
    }

    public static String cursorOf(Window<?> pagination) {
        return cursorOf(pagination, "id");
    }

    public static String cursorOf(Window<?> pagination, String key) {
        if (pagination.isEmpty()) {
            return null;
        }

        KeysetScrollPosition position = (KeysetScrollPosition) pagination.positionAt(pagination.getContent().size() - 1);
        return cursorOf(position, key);
    }

    public static String cursorOf(KeysetScrollPosition position) {
        return cursorOf(position, "id");
    }

    public static String cursorOf(@NonNull KeysetScrollPosition position, String key) {
        return position.getKeys().get(key).toString();
    }
}
