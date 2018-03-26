package io.dashbase.parser.ast;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExprBinder {
    ExprType type();
}
