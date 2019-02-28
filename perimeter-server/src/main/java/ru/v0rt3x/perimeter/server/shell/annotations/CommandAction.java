package ru.v0rt3x.perimeter.server.shell.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CommandAction {

    String value();
}
