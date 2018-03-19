package lexer.state;

import lexer.Lexer;

import java.util.Objects;

import static java.lang.String.format;
import static lexer.state.LexerStates.*;
import static token.ItemType.*;
import static utils.NumberUtils.isAlpha;
import static utils.NumberUtils.isDigit;

@StatesBinder(binder = LexStatements)
public class LexStatements extends State {

    @Override
    public LexerStates nextTo(Lexer lexer) {
        if (lexer.isBraceOpen()) {
            return LexInsideBrace;
        }

        if (lexer.left().startsWith("#")) {
            return LexLineComment;
        }

        // line comment

        // EOF
        Character c = lexer.next();
        if (Objects.isNull(c)) {
            if (lexer.getParenDepth() != 0) {
                return lexer.error("unclosed left parenthesis");
            } else if (lexer.isBracketOpen()) {
                return lexer.error("unclosed left bracket");
            }

            lexer.emit(itemEOF);
            // empty state
            return null;
        }

        // Space
        if (Character.isWhitespace(c)) {
            return LexSpace;
        }

        // digits
        else if (isDigit(c) || (c == '.' && isDigit(lexer.peek()))) {
            lexer.backup();
            return LexNumberOrDuration;
        }

        // keyword (NaN as number in this fork)
        else if (isAlpha(c) || c == ':') {
            lexer.backup();
            return LexKeywordOrIdentifier;
        }

        switch (c) {
            case ',': {
                lexer.emit(itemComma);
                break;
            }
            case '*': {
                lexer.emit(itemMUL);
                break;
            }
            case '/': {
                lexer.emit(itemDIV);
                break;
            }
            case '%': {
                lexer.emit(itemMOD);
                break;
            }
            case '+': {
                lexer.emit(itemADD);
                break;
            }
            case '-': {
                lexer.emit(itemSUB);
                break;
            }
            case '^': {
                lexer.emit(itemPOW);
                break;
            }
            case '(': {
                lexer.emit(itemLeftParen);
                // deep++
                lexer.setParenDepth(lexer.getParenDepth() + 1);
                return LexStatements;
            }
            case ')': {
                lexer.emit(itemRightParen);
                lexer.setParenDepth(lexer.getParenDepth() - 1);
                if (lexer.getParenDepth() < 0) {
                    return lexer.error("unexpected right parenthesis %c", c);
                }
                return LexStatements;
            }
            case '{': {
                lexer.emit(itemLeftBrace);
                lexer.setBraceOpen(true);
                return LexInsideBrace;
            }

            case '[': {
                if (lexer.isBracketOpen()) {
                    return lexer.error("unexpected left bracket %c", c);
                }

                lexer.emit(itemLeftBracket);
                lexer.setBracketOpen(true);
                return LexDuration;
            }

            case ']': {
                if (!lexer.isBracketOpen()) {
                    return lexer.error("unexpected right bracket %c", c);
                }

                lexer.emit(itemRightBracket);
                lexer.setBracketOpen(false);
                break;
            }

            case '\"':
            case '\'': {
                lexer.setStringOpen(c);
                return LexString;
            }

            case '`': {
                lexer.setStringOpen(c);
                return LexRawString;
            }

            case '=': {
                Character next = lexer.peek();
                if (Objects.nonNull(next) && next == '=') {
                    lexer.next();
                    lexer.emit(itemEQL);
                } else if (Objects.nonNull(next) && next == '~') {
                    return lexer.error("unexpected character after '=': %c", next);
                } else {
                    lexer.emit(itemAssign);
                }

                break;
            }

            case '!': {
                Character next = lexer.next();
                if (Objects.nonNull(next) && next == '=') {
                    lexer.emit(itemNEQ);
                } else {
                    return lexer.error("unexpected character after '!': %c", next);
                }

                break;
            }

            case '<': {
                Character ch = lexer.peek();
                if (Objects.nonNull(ch) && ch == '=') {
                    lexer.next();
                    lexer.emit(itemLTE);
                } else {
                    lexer.emit(itemLSS);
                }

                break;
            }

            case '>': {
                Character ch = lexer.next();
                if (Objects.nonNull(ch) && ch == '=') {
                    lexer.next();
                    lexer.emit(itemGTE);
                } else {
                    lexer.emit(itemGTR);
                }
            }

            default: {
                return lexer.error("unexpected character: %c", c);
            }
        }

        return LexStatements;
    }
}