package tech.bjut.su.appeal.security;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

@Getter
@EqualsAndHashCode
public class ResourceAuthority implements GrantedAuthority {

    private final String entityName, entityId;

    public ResourceAuthority(String entityName, String entityId) {
        this.entityName = entityName;
        this.entityId = entityId;
    }

    @Override
    public String getAuthority() {
        // complex authority, should not be represented as a string
        return null;
    }

    public static final String ENTITY_NAME_QUESTION = "question";
}
