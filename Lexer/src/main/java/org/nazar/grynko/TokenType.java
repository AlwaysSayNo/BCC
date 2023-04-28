package org.nazar.grynko;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Arrays;

@AllArgsConstructor
@Getter
@Accessors(fluent = true)
public enum TokenType {

    // Other
    IDENTIFIER ("identifier"),
    INVALID ("invalid"),

    // Punctuation
    PUNCTUATION ("punctuation"),

    // Spaces
    WHITE_SPACE ("white_space"),
    TAB ("tab"),

    // Comments
    SINGLE_LINE_COMMENT ("single_line_comment"),
    MULTILINE_COMMENT ("multiline_comment"),

    // Types
    STRING_LITERAL ("string_literal"),
    INT ("int"),
    FLOAT ("float"),

    // Keywords
    ASSOCIATIVITY ("associativity"),
    BREAK ("break"),
    CASE ("case"),
    CATCH ("catch"),
    CLASS ("class"),
    CONTINUE ("continue"),
    CONVENIENCE ("convenience"),
    DEFAULT ("default"),
    DEFER ("defer"),
    DE_INIT ("deinit"),
    DID_SET ("didSet"),
    DO ("do"),
    DYNAMIC ("dynamic"),
    ELSE ("else"),
    ENUM ("enum"),
    EXTENSION ("extension"),
    FALLTHROUGH ("fallthrough"),
    FALSE ("false"),
    FILE_PRIVATE ("fileprivate"),
    FINAL ("final"),
    FOR ("for"),
    FUNC ("func"),
    GET ("get"),
    GUARD ("guard"),
    IF ("if"),
    IMPORT ("import"),
    IN ("in"),
    INDIRECT ("indirect"),
    INFIX ("infix"),
    INIT ("init"),
    INOUT ("inout"),
    INTERNAL ("internal"),
    LAZY ("lazy"),
    LEFT ("left"),
    LET ("let"),
    MUTATING ("mutating"),
    NIL ("nil"),
    NONE ("none"),
    NONM_UTATING ("nonmutating"),
    OPERATOR ("operator"),
    OPTIONAL ("optional"),
    OVERRIDE ("override"),
    POSTFIX ("postfix"),
    PRECEDENCE ("precedence"),
    PREFIX ("prefix"),
    PRIVATE ("private"),
    PROTOCOL ("protocol"),
    PUBLIC ("public"),
    REPEAT ("repeat"),
    REQUIRED ("required"),
    RETHROWS ("rethrows"),
    RETURN ("return"),
    RIGHT ("right"),
    SAFE ("safe"),
    SELF ("self"),
    SET ("set"),
    STATIC ("static"),
    STRUCT ("struct"),
    SUBSCRIPT ("subscript"),
    SUPER ("super"),
    SWITCH ("switch"),
    THROW ("throw"),
    THROWS ("throws"),
    TRUE ("true"),
    TYPE_ALIAS ("typealias"),
    UNOWNED ("unowned"),
    VAR ("var"),
    WEAK ("weak"),
    WHERE ("where"),
    WHILE ("while"),
    WILL_SET ("willSet"),

    // Operators
    ADDITION ("+"),
    SUBTRACTION ("-"),
    MULTIPLICATION ("*"),
    DIVISION ("/"),
    REMAINDER ("%"),
    BITWISE_AND ("&"),
    BITWISE_OR ("|"),
    BITWISE_XOR ("^"),
    BITWISE_NOT ("~"),
    LEFT_SHIFT ("<<"),
    RIGHT_SHIFT (">>"),
    POWER ("**"),
    HALF_OPEN_RANGE_OPERATOR ("..<"),
    CLOSED_RANGE_OPERATOR ("..."),
    LESS_THAN ("<"),
    GREATER_THAN (">"),
    LESS_THAN_OR_EQUAL_TO ("<="),
    EQUAL_TO ("=="),
    NOT_EQUAL_TO ("!="),
    IDENTITY_NOT_EQUAL_TO ("!=="),
    LOGICAL_AND ("&&"),
    LOGICAL_OR ("||"),
    NOT ("!"),
    TERNARY_CONDITIONAL (":"),
    QUESTION_MARK ("?"),
    ADDITION_ASSIGNMENT ("+="),
    SUBTRACTION_ASSIGNMENT ("-="),
    MULTIPLICATION_ASSIGNMENT ("*="),
    DIVISION_ASSIGNMENT ("/="),
    REMAINDER_ASSIGNMENT ("%="),
    BITWISE_AND_ASSIGNMENT ("&="),
    BITWISE_OR_ASSIGNMENT ("|="),
    BITWISE_XOR_ASSIGNMENT ("^="),
    RIGHT_SHIFT_ASSIGNMENT (">>="),
    LOGICAL_AND_ASSIGNMENT ("&&="),
    LOGICAL_OR_ASSIGNMENT ("||="),
    NIL_COALESCING_ASSIGNMENT ("??="),
    ASSIGNMENT ("="),
    LEFT_SHIFT_ASSIGNMENT ("<<="),
    IDENTITY_EQUALITY ("==="),
    THREE_WAY_COMPARISON ("<=>"),
    TYPE_CHECKING ("as"),
    OPTIONAL_TYPE_CAST ("as?"),
    FORCED_TYPE_CAST ("as!"),
    ERROR_HANDLING ("try"),
    OPTIONAL_ERROR_HANDLING ("try?"),
    FORCED_ERROR_HANDLING ("try!"),
    DOT ("."),
    OPTIONAL_ASSIGNMENT ("?="),
    TYPE_CHECKING_IS ("is"),
    GREATER_THAN_OR_EQUAL_TO (">="),
    NIL_COALESCING_OPERATOR ("??"),
    POWER_OPERATOR ("**="),

    // Other
    COMMA (","),
    SEMICOLON (";"),
    LEFT_PARENTHESIS (")"),
    RIGHT_PARENTHESIS (")"),
    LEFT_BRACKET ("["),
    RIGHT_BRACKET ("]"),
    LEFT_CURLY_BRACE ("{");

    private String value;

    public static TokenType getByValue(String val) {
        return Arrays.stream(TokenType.values())
                .filter(type -> type.value().equals(val))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Illegal token: " + val));
    }

}
