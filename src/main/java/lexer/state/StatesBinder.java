package lexer.state;

import java.lang.annotation.*;

import static lexer.state.LexerStates.LexStatements;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface StatesBinder {
    LexerStates binder() default LexStatements;
}
