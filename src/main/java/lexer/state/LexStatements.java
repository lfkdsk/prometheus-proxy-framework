package lexer.state;

import exception.ParserException;
import lexer.Lexer;

import java.util.Objects;

import static java.lang.String.format;
import static lexer.state.LexerStates.*;
import static token.ItemType.*;
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
                throw new ParserException("unclosed left parenthesis");
            } else if (lexer.isBracketOpen()) {
                throw new ParserException("unclosed left bracket");
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
        if (isDigit(c) || (c == '.' && isDigit(lexer.peek()))) {
            lexer.backup();
            return LexNumberOrDuration;
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
                    throw new ParserException(format("unexpected right parenthesis %c", c));
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
                    throw new ParserException(format("unexpected left bracket %c", c));
                }

                lexer.emit(itemLeftBracket);
                lexer.setBracketOpen(true);
                return LexDuration;
            }

            case ']': {
                if (!lexer.isBracketOpen()) {
                    throw new ParserException(format("unexpected right bracket %c", c));
                }

                lexer.emit(itemRightBracket);
                lexer.setBracketOpen(false);
                break;
            }
            default: {
                throw new ParserException(format("unexpected character: %c", c));
            }
        }

        return LexStatements;
    }
}