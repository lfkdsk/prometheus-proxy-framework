package io.dashbase.web.utils;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TextStateBinder {
    TextStates binder() default TextStates.TextTerminal;
}
