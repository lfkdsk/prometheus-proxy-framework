package io.dashbase.lexer.state;

import io.dashbase.lexer.QueryLexer;
import io.dashbase.lexer.token.ItemType;

import java.util.Objects;

import static io.dashbase.lexer.state.LexerStates.*;
import static io.dashbase.lexer.state.States.statementsMap;
import static io.dashbase.utils.TypeUtils.isAlpha;
import static io.dashbase.utils.TypeUtils.isDigit;
import static io.dashbase.utils.TypeUtils.isSpace;

@StatesBinder(binder = LexValueSequence)
public class LexValueSequence extends State {

    @Override
    public LexerStates nextTo(QueryLexer lexer) {
        Character ch = lexer.next();
        if (Objects.isNull(ch)) {
            return LexStatements;
        }

        // space
        if (isSpace(ch)) {
            statementsMap.get(LexSpace).nextTo(lexer);

            return LexValueSequence;
        }


        switch (ch) {
            case '+': {
                lexer.emit(ItemType.itemADD);
                break;
            }

            case '-': {
                lexer.emit(ItemType.itemSUB);
                break;
            }

            case '_': {
                lexer.emit(ItemType.itemBlank);
                break;
            }

            case 'x': {
                lexer.emit(ItemType.itemTimes);
                break;
            }

            default: {
                // digit
                if (isDigit(ch) || (ch == '.' && isDigit(lexer.peek()))) {
                    lexer.backup();
                    statementsMap.get(LexNumber).nextTo(lexer);

                    return LexValueSequence;
                }

                // alpha
                else if (isAlpha(ch)) {
                    lexer.backup();
                    // We might lex invalid items here but this will be caught by the parser.
                    return LexKeywordOrIdentifier;
                } else {
                    return lexer.error("unexpected character in series sequence: %c", ch);
                }
            }
        }

        return LexValueSequence;
    }
}
