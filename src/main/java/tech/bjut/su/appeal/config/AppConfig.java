package tech.bjut.su.appeal.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;

import java.util.Locale;

@Configuration
@EnableCaching
@EnableConfigurationProperties(AppProperties.class)
public class AppConfig {

    @Bean
    public LocaleResolver localeResolver() {
        FixedLocaleResolver localeResolver = new FixedLocaleResolver();
        localeResolver.setDefaultLocale(new Locale("zh", "CN"));
        return localeResolver;
    }
}
