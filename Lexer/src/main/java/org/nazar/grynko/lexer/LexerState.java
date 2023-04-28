package org.nazar.grynko.lexer;

public enum LexerState {

    DEFAULT,
    ERROR,
    LONG_COMMENT,
    LONG_COMMENT_ENDING,
    INTEGER,
    FLOAT,
    STRING,
    STRING_SLASH,
    IDENTIFIER,
    OPERATOR,
    KEYWORD,
    WHITE_SPACE,
    TAB

}
