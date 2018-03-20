package lexer.state;

import lexer.QueryLexer;
import model.token.ItemType;

import java.util.Objects;

import static lexer.state.LexerStates.*;
import static lexer.state.States.statementsMap;
import static utils.TypeUtils.isAlpha;
import static utils.TypeUtils.isDigit;
import static utils.TypeUtils.isSpace;

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
