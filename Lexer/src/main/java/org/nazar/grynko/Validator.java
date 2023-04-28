package org.nazar.grynko;

public interface Validator {

    static boolean isWhitespace(char c) {
        return c == ' ';
    }

    static boolean isTab(char c) {
        return c == '\t';
    }

    static boolean isPunctuation(Character c) {
        return ",;[]{}()".contains(c.toString());
    }

    static boolean isDigit(char c) {
        return Character.isDigit(c);
    }

    static boolean isWord(char c) {
        return Character.isAlphabetic(c) || c == '_';
    }

    static boolean isOperator(Character c) {
        return "+-*/<>=?!.:&|^~%".contains(c.toString());
    }

    static boolean isDot(char c) {
        return c == '.';
    }

    static boolean isDoubleQuote(char c) {
        return c == '"';
    }

    static boolean isCommentOpen(char c1, char c2) {
        return c1 == '/' && (c2 == '/' || c2 == '*');
    }

    static boolean isEndOfToken(char c) {
        return isWhitespace(c) || isTab(c) || isPunctuation(c) || isOperator(c);
    }

}
