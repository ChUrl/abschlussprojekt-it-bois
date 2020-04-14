package mops.gruppen2.config.converter;

import mops.gruppen2.domain.model.group.wrapper.Limit;
import org.springframework.core.convert.converter.Converter;

public class StringToLimitConverter implements Converter<String, Limit> {

    @Override
    public Limit convert(String value) {
        return new Limit(Long.parseLong(value));
    }
}
