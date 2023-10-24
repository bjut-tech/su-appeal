package tech.bjut.su.appeal.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.Filter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tech.bjut.su.appeal.filter.TrailingSlashRedirectFilter;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ObjectMapper objectMapper;

    public WebConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public Filter trailingSlashRedirectFilter() {
        return new TrailingSlashRedirectFilter();
    }

    @Bean
    public FilterRegistrationBean<Filter> trailingSlashFilter() {
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(trailingSlashRedirectFilter());
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }

    @PostConstruct
    public void EnumObjectMapper() {
        objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
    }
}
