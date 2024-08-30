package tech.bjut.su.appeal.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public class StringToEnumConverterFactory implements ConverterFactory<String, Enum<?>> {

    @NonNull
    @Override
    public <T extends Enum<?>> Converter<String, T> getConverter(@NonNull Class<T> targetType) {
        return new StringToEnumConverter<>(targetType);
    }

    private static class StringToEnumConverter<T extends Enum<?>> implements Converter<String, T> {

        private final Class<T> enumType;

        public StringToEnumConverter(Class<T> enumType) {
            this.enumType = enumType;
        }

        @Nullable
        @Override
        public T convert(String source) {
            source = StringUtils.stripToNull(source);
            if (source == null) {
                return null;
            }
            source = source.toUpperCase();

            for (T enumConstant : enumType.getEnumConstants()) {
                if (enumConstant.name().equals(source)) {
                    return enumConstant;
                }
            }
            return null;
        }
    }
}
