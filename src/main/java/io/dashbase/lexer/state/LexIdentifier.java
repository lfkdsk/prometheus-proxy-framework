package io.dashbase.lexer.state;

import io.dashbase.lexer.QueryLexer;
import io.dashbase.lexer.token.ItemType;

import static io.dashbase.utils.TypeUtils.isAlphaNumeric;
import static io.dashbase.utils.TypeUtils.isKeyWordOrIdentifier;

@StatesBinder(binder = LexerStates.LexIdentifier)
public class LexIdentifier extends State {
    // lexIdentifier scans an alphanumeric identifier. The next character
    // is known to be a letter.
    @Override
    public LexerStates nextTo(QueryLexer lexer) {
        for (;isKeyWordOrIdentifier(lexer.next()););
        lexer.backup();
        lexer.emit(ItemType.itemIdentifier);

        return LexerStates.LexStatements;
    }
}
