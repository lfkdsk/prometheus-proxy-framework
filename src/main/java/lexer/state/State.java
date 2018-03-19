package lexer.state;

import lexer.Lexer;
import lombok.Getter;

public abstract class State {

    @Getter
    private LexerStates lexerStates;

    State() {
        StatesBinder binder = this.getClass().getAnnotation(StatesBinder.class);
        if (binder == null) {
            throw new IllegalArgumentException("State Class Should bind LexerStates");
        }

        this.lexerStates = binder.binder();
    }

    abstract public LexerStates nextTo(Lexer lexer);
}
