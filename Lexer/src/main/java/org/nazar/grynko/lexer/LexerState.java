package org.nazar.grynko.lexer;

public enum LexerState {

    DEFAULT,
    ERROR,
    SINGLE_LINE_COMMENT,
    MULTILINE_COMMENT,
    MULTILINE_COMMENT_ENDING,
    INTEGER,
    FLOAT,
    SINGLE_LINE_STRING,
    MULTILINE_STRING,
    STRING_SLASH,
    IDENTIFIER,
    OPERATOR,
    KEYWORD,
    PUNCTUATION,
    WHITE_SPACE,
    TAB,
    TYPE_DECLARATION

}
