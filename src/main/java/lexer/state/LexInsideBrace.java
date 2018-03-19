package lexer.state;

import lexer.Lexer;

import java.util.Objects;

import static java.lang.String.format;
import static lexer.state.LexerStates.*;
import static token.ItemType.*;

@StatesBinder(binder = LexInsideBrace)
public class LexInsideBrace extends State{
    @Override
    public LexerStates nextTo(Lexer lexer) {
        if (lexer.left().startsWith("#")) {
            return LexLineComment;
        }

        Character ch = lexer.next();
        if (Objects.isNull(ch)) {
            return lexer.error("unexpected end of input inside braces");
        } else if (Character.isWhitespace(ch)) {
            return LexSpace;
        } else if (Character.isAlphabetic(ch)) {
            return LexIdentifier;
        }

        switch (ch) {
            case ',': {
                lexer.emit(itemComma);
                break;
            }
            case '\"':
            case '\'': {
                lexer.setStringOpen(ch);
                return LexString;
            }

            case '`': {
                lexer.setStringOpen(ch);
                return LexRawString;
            }

            case '=': {
                Character next = lexer.peek();
                if (next == '~') {
                    // Regex
                    lexer.emit(itemEQLRegex);
                    break;
                }

                lexer.backup();
                lexer.emit(itemEOF);
                break;
            }
            case '!': {
                Character next = lexer.peek();
                if (next == '~') {
                    lexer.emit(itemNEQRegex);
                } else if (next == '=') {
                    lexer.emit(itemNEQ);
                } else {
                    return lexer.error("unexpected character after '!' inside braces: %c", next);
                }
                break;
            }

            case '{': {
                return lexer.error("unexpected left brace %c", ch);
            }

            case '}': {
                lexer.emit(itemRightBrace);
                // Close brace
                lexer.setBraceOpen(false);

                if (lexer.isSeriesDesc()) {
                    return LexValueSequence;
                }

                return LexStatements;
            }

            default: {
                return lexer.error("unexpected character inside braces: %c", ch);
            }
        }
        return LexInsideBrace;
    }
}
