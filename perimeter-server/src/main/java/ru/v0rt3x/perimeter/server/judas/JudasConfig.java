package ru.v0rt3x.perimeter.server.judas;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class JudasConfig {

    private String pattern;

    @JsonGetter("pattern")
    public String getPattern() {
        return pattern;
    }

    @JsonIgnore
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}
