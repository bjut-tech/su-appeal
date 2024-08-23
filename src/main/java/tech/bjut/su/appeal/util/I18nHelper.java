package tech.bjut.su.appeal.util;

import org.springframework.context.MessageSource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

@Component
public class I18nHelper {

    private final MessageSource messageSource;

    private final LocaleResolver localeResolver;

    public I18nHelper(
        MessageSource messageSource,
        LocaleResolver localeResolver
    ) {
        this.messageSource = messageSource;
        this.localeResolver = localeResolver;
    }

    public String get(String key) {
        return get(key, null);
    }

    public String get(String key, @Nullable Object[] args) {
        // noinspection DataFlowIssue: we are using a FixedLocaleResolver
        Locale locale = localeResolver.resolveLocale(null);
        return messageSource.getMessage(key, args, locale);
    }
}
