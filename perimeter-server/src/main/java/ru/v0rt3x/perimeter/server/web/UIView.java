package ru.v0rt3x.perimeter.server.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UIView {
    String name();
    String link();
    int linkOrder() default 0;
    String title() default "";
    String icon() default "dashboard";
}
