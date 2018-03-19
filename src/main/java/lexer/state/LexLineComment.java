package lexer.state;

import lexer.Lexer;
import token.ItemType;

import java.util.Objects;

import static utils.NumberUtils.isEndOfLine;

@StatesBinder(binder = LexerStates.LexLineComment)
public class LexLineComment extends State {

    @Override
    public LexerStates nextTo(Lexer lexer) {
        lexer.setPosition(lexer.getPosition() + "#".length());
        for (Character ch = lexer.next();
             Objects.nonNull(ch) && !isEndOfLine(ch);
             ch = lexer.next())
            ;

        lexer.backup();
        lexer.emit(ItemType.itemComment);
        return LexerStates.LexStatements;
    }
}
