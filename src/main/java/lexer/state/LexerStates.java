package lexer.state;

public enum LexerStates {
    LexStatements,
    LexSpace,
    LexInsideBrace,
    LexLineComment,
    LexIdentifier,
    LexString,
    LexRawString,
    LexValueSequence,
    LexDuration,
    LexNumberOrDuration,
    LexNumber,
    LexKeywordOrIdentifier,
    LexEscape,
}
