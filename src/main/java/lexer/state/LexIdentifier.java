package lexer.state;

import lexer.Lexer;
import token.ItemType;

import static utils.NumberUtils.isAlphaNumeric;

@StatesBinder(binder = LexerStates.LexIdentifier)
public class LexIdentifier extends State {
    // lexIdentifier scans an alphanumeric identifier. The next character
    // is known to be a letter.
    @Override
    public LexerStates nextTo(Lexer lexer) {
        for (;isAlphaNumeric(lexer.next()););
        lexer.backup();
        lexer.emit(ItemType.itemIdentifier);

        return LexerStates.LexStatements;
    }
}
