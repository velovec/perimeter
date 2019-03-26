package ru.v0rt3x.perimeter.server.utils;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class YAMLUtils {

    public static <T> Yaml getParser(Class<T> target) {
        Constructor constructor = new Constructor(target);

        constructor.setPropertyUtils(new YAMLPropertyUtils());

        return new Yaml(constructor);
    }

    public static class YAMLPropertyUtils extends PropertyUtils {
        @Override
        public Property getProperty(Class<?> type, String name, BeanAccess bAccess) {
            if (name.indexOf('-') > -1) {
                name = Arrays.stream(name.split("-"))
                    .map(String::toLowerCase)
                    .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1))
                    .collect(Collectors.joining());
            }

            return super.getProperty(type, name, bAccess);
        }
    }
}
