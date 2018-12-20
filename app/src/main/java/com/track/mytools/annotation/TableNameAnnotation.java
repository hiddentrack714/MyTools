package com.track.mytools.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.TYPE;

@Retention(RUNTIME)
@Target({TYPE})
public @interface TableNameAnnotation {
    public String value();
}
