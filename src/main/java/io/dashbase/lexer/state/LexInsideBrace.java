package io.dashbase.lexer.state;

import io.dashbase.lexer.QueryLexer;

import java.util.Objects;

import static java.lang.String.format;
import static io.dashbase.lexer.state.LexerStates.*;
import static io.dashbase.lexer.token.ItemType.*;
import static io.dashbase.utils.TypeUtils.isAlpha;
import static io.dashbase.utils.TypeUtils.isSpace;

@StatesBinder(binder = LexInsideBrace)
public class LexInsideBrace extends State{
    @Override
    public LexerStates nextTo(QueryLexer lexer) {
        if (lexer.left().startsWith("#")) {
            return LexLineComment;
        }

        Character ch = lexer.next();
        if (Objects.isNull(ch)) {
            return lexer.error("unexpected end of input inside braces");
        } else if (isSpace(ch)) {
            return LexSpace;
        } else if (isAlpha(ch)) {
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
                Character next = lexer.next();
                if (Objects.nonNull(next) && next == '~') {
                    // Regex
                    lexer.emit(itemEQLRegex);
                    break;
                }

                lexer.backup();
                lexer.emit(itemEQL);
                break;
            }
            case '!': {
                Character next = lexer.next();
                if (Objects.nonNull(next) && next == '~') {
                    lexer.emit(itemNEQRegex);
                } else if (Objects.nonNull(next) && next == '=') {
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
                return lexer.error("unexpected character inside braces: \'%c\'", ch);
            }
        }
        return LexInsideBrace;
    }
}
