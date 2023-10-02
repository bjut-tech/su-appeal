package tech.bjut.su.appeal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("app")
@Getter
@Setter
public class AppProperties {

    private final Auth auth = new Auth();

    @Getter
    @Setter
    public static class Auth {

        private List<String> admin;

        private String jwtSecret;

    }

    private final Store store = new Store();

    @Getter
    @Setter
    public static class Store {

        private String path;

    }

}
