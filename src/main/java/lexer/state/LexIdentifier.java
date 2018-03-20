package lexer.state;

import lexer.QueryLexer;
import model.token.ItemType;

import static utils.TypeUtils.isAlphaNumeric;

@StatesBinder(binder = LexerStates.LexIdentifier)
public class LexIdentifier extends State {
    // lexIdentifier scans an alphanumeric identifier. The next character
    // is known to be a letter.
    @Override
    public LexerStates nextTo(QueryLexer lexer) {
        for (;isAlphaNumeric(lexer.next()););
        lexer.backup();
        lexer.emit(ItemType.itemIdentifier);

        return LexerStates.LexStatements;
    }
}
