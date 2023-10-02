package tech.bjut.su.appeal.config;

import jakarta.servlet.Filter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.bjut.su.appeal.filter.TrailingSlashRedirectFilter;

@Configuration
public class WebConfig {

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
}
