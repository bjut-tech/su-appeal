package tech.bjut.su.appeal.util;

import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.lang.NonNull;

import java.util.Map;

public class CursorPaginationHelper {

    /**
     * Cursor string to `KeysetScrollPosition` without pinned
     */
    public static KeysetScrollPosition positionOf(String cursor) {
        return positionOf(cursor, false);
    }

    /**
     * Cursor string to `KeysetScrollPosition`
     */
    public static KeysetScrollPosition positionOf(String cursor, boolean withPinned) {
        if (cursor == null || cursor.isBlank()) {
            return ScrollPosition.keyset();
        } else if (withPinned) {
            boolean pinned = cursor.charAt(0) == '-';
            if (pinned) {
                cursor = cursor.substring(1);
            }
            return ScrollPosition.of(Map.of("id", cursor, "pinned", pinned), ScrollPosition.Direction.FORWARD);
        } else {
            return ScrollPosition.of(Map.of("id", cursor), ScrollPosition.Direction.FORWARD);
        }
    }

    /**
     * `Window` to cursor string without pinned
     */
    public static String cursorOf(Window<?> pagination) {
        return cursorOf(pagination, false);
    }

    /**
     * `Window` to cursor string
     */
    public static String cursorOf(Window<?> pagination, boolean withPinned) {
        if (pagination.isEmpty()) {
            return null;
        }

        KeysetScrollPosition position = (KeysetScrollPosition) pagination.positionAt(pagination.getContent().size() - 1);
        return cursorOf(position, withPinned);
    }

    /**
     * `KeysetScrollPosition` to cursor string
     */
    public static String cursorOf(@NonNull KeysetScrollPosition position, boolean withPinned) {
        Map<String, Object> keys = position.getKeys();
        if (withPinned) {
            return (boolean) keys.get("pinned") ? "-" + keys.get("id") : keys.get("id").toString();
        } else {
            return keys.get("id").toString();
        }
    }
}
