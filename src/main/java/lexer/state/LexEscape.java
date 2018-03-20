package lexer.state;

import lexer.QueryLexer;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@StatesBinder(binder = LexerStates.LexEscape)
public class LexEscape extends State {
    @Override
    public LexerStates nextTo(QueryLexer lexer) {
        Character ch = lexer.next();

        if (Objects.isNull(ch)) {
            lexer.error("escape sequence not terminated");
            return null;
        }

        List<Character> converter = Arrays.asList('a', 'b', 'f', 'n', 'r', 't', 'v', '\\', lexer.getStringOpen());
        if (converter.contains(ch)) {
            // useless
            return null;
        }

        int n = 0, base = 0, max = 0;
        converter = Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7');
        if (converter.contains(ch)) {
            n = 3;
            base = 8;
            max = 255;
        } else if (ch == 'x') {
            ch = lexer.next();
            n = 2;
            base = 16;
            max = 255;
        } else if (ch == 'u') {
            ch = lexer.next();
            n = 4;
            base = 16;
            max = Integer.MAX_VALUE;
        } else if (ch == 'U') {
            ch = lexer.next();
            n = 8;
            base = 16;
            max = Integer.MAX_VALUE;
        } else {
            lexer.error("unknown escape sequence \'%c\'", ch);
        }

        int x = 0;
        for (;n > 0;) {
            int d = digitVal(ch);
            if (d >= base) {
                if (Objects.isNull(ch)) {
                    lexer.error("escape sequence not terminated");
                }

                lexer.error("illegal character \'%c\' in escape sequence", ch);
            }

            x = x * base + d;
            ch = lexer.next();
            n--;
        }

        if (x > max || 0xD800 <= x && x < 0xE000) {
            lexer.error("escape sequence is an invalid Unicode code point");
        }

        return null;
    }

    private int digitVal(Character ch) {
        if('0' <= ch && ch <= '9') {
            return ch - '0';
        } else if ('a' <= ch && ch <= 'f') {
            return ch - 'a' + 10;
        } else if ('A' <= ch && ch <= 'F') {
            return ch - 'A' + 10;
        }

        return 16; // Larger than any legal digit val.
    }
}
