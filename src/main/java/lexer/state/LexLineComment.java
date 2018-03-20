package lexer.state;

import lexer.QueryLexer;
import model.token.ItemType;

import java.util.Objects;

import static utils.TypeUtils.isEndOfLine;

@StatesBinder(binder = LexerStates.LexLineComment)
public class LexLineComment extends State {

    @Override
    public LexerStates nextTo(QueryLexer lexer) {
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
