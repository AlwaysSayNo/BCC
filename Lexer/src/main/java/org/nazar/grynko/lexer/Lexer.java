package org.nazar.grynko.lexer;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.nazar.grynko.*;
import org.nazar.grynko.automate.Automate;
import org.nazar.grynko.automate.AutomateBuilder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.nazar.grynko.Validator.*;

@Getter
@Setter
public class Lexer {

    private final Automate operatorsAutomate;
    private final Automate keywordsAutomate;

    private LexerState state;
    private List<Token> tokens;
    private List<InvalidToken> invalid;
    private LineCursor cursor;
    private List<String> symbolTable;
    private StringBuilder cache;

    public Lexer(List<String> operators, List<String> keywords) {
        operatorsAutomate = AutomateBuilder.build(operators);
        keywordsAutomate = AutomateBuilder.build(keywords);

        state = LexerState.DEFAULT;
        tokens = new ArrayList<>();
        invalid = new ArrayList<>();
        symbolTable = new ArrayList<>();
        cache = new StringBuilder();
    }

    public boolean parse(String path) {
        try {
            parseInternal(path);
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    @SneakyThrows
    private void parseInternal(String path) {
        var reader = new BufferedReader(new FileReader(path));

        String line;
        cursor = new LineCursor();
        while ((line = reader.readLine()) != null) {
            cursor.line(line)
                    .col(0);

            while (!cursor.isEnded()) {
                processToken();
            }

            cursor.row(cursor.row() + 1);
        }
    }

    private void processToken() {
        var symbol = cursor.nextChar();

        if (isWhitespace(symbol)) {
            processWhitespace();
            return;
        } else if (isTab(symbol)) {
            processTab();
            return;
        } else if (isDigit(symbol)) {
            processNumber();
            return;
        } else if (isWord(symbol)) {
            processWord();
            return;
        } else if (isOperator(symbol)) {
            processOperator();
            return;
        } else if (isDoubleQuote(symbol)) {
            processDoubleQuote();
            return;
        } else if (isAt(symbol)) {
            processTypeDeclaration();
            return;
        }
//        else if (!cursor.isEnded(1) && isCommentOpen(symbol, cursor.nextChar(1))) {
//            processComment();
//        }


        // multiline
        // comment
        // dot - can be function or float
        // interpolation

        processBadToken(cursor.nextChar());
    }

    private void processWhitespace() {
        state = LexerState.WHITE_SPACE;

        int shift = read(Validator::isWhitespace, 0);
        addToken(TokenType.WHITE_SPACE, shift);
    }

    private void processTab() {
        state = LexerState.TAB;

        int shift = read(Validator::isTab, 0);
        addToken(TokenType.TAB, shift);
    }

    private void processNumber() {
        state = LexerState.INTEGER;

        int shift = read(Validator::isDigit, 0);

        if (!cursor.isEnded(shift) && isDot(cursor.nextChar(shift))) {
            state = LexerState.FLOAT;
            shift++;
        }

        if (!cursor.isEnded(shift)) {
            shift = read(Validator::isDigit, shift);
        }

        if (cursor.isEnded(shift) || isEndOfToken(cursor.nextChar(shift))) {
            var tokenType = state == LexerState.INTEGER ? TokenType.INT : TokenType.FLOAT;
            addToken(tokenType, shift);
        } else {
            processBadToken(shift);
        }
    }

    private void processWord() {
        state = LexerState.IDENTIFIER;

        int shift = read((Character c) -> !isEndOfToken(c), 0);
        var col = cursor.col();
        var word = cursor.line().substring(col, col + shift);
        TokenType type;

        // Check if it is a "as"/"try" + ("?"/"!")
        if (isAlphabeticOperator(word)) {
            if (!cursor.isEnded(shift) && isCastOperator(cursor.nextChar(shift))) {
                word += cursor.nextChar(shift);
                shift++;
            }
            type = operatorsAutomate.getType(word);
            if (type != TokenType.INVALID) {
                state = LexerState.OPERATOR;
            }
        }
        else {
            type = keywordsAutomate.getType(word);
            if (type != TokenType.INVALID) {
                state = LexerState.KEYWORD;
            }
        }

        if (type == TokenType.INVALID) {
            addToken(TokenType.IDENTIFIER, shift);
        }
        else {
            addToken(type, shift);
        }
    }

    private void processOperator() {
        state = LexerState.OPERATOR;

        int shift = read(Validator::isOperator, 0);
        var col = cursor.col();
        var word = cursor.line().substring(col, col + shift);

        var type = operatorsAutomate.getType(word);
        if (type == TokenType.INVALID) {
            processBadToken(shift);
        }
        else {
            addToken(type, shift);
        }
    }

    private void processDoubleQuote() {
        state = LexerState.STRING;

        int shift = 0;
        char symbol;

        while (!cursor.isEnded(shift + 1)) {
            shift++;
            symbol = cursor.nextChar(shift);

            if (state == LexerState.STRING) {
                if (symbol == '\\') {
                    state = LexerState.STRING_SLASH;
                }
                else if (symbol == '\"') {
                    addToken(TokenType.STRING_LITERAL, shift + 1);
                    return;
                }
            }
            else if (state == LexerState.STRING_SLASH) {
                state = LexerState.STRING;
            }
        }

        var message = String.format(
                "No closing quote was found for the first quote \" (%d, %d)", cursor.row(), cursor.col());
        processBadToken(message, shift);
    }

    private void processTypeDeclaration() {
        state = LexerState.TYPE_DECLARATION;

        int shift = read(Validator::isWord, 1);

        // we read only @ sign - incorrect token
        if (shift == 1) {
            processBadToken(shift);
            return;
        }

        addToken(TokenType.TYPE_DECLARATION, shift);
    }

    private void processBadToken(Character c) {
        addInvalid(c.toString(), "Invalid character", 1);
    }

    private void processBadToken(int shift) {
        processBadToken("Invalid token", shift);
    }

    private void processBadToken(String message, int shift) {
        state = LexerState.ERROR;

        int col = cursor.col();

        if (shift == 0 && cursor.line().length() == col + 1) {
            shift++;
        } else if (!cursor.isEnded(shift)) {
            shift = read((Character c) -> !isEndOfToken(c), shift);
        }

        addInvalid(cursor.line().substring(col, col + shift), message, shift);
    }

    private void addInvalid(String token, String message, int shift) {
        int row = cursor.row(), col = cursor.col();
        invalid.add(new InvalidToken(token, message, row, col));
        cursor.col(col + shift);
        state = LexerState.DEFAULT;
    }

    private void addToken(TokenType type, int length) {
        add(type, length, "");
    }

    private void add(TokenType type, int length, String prefix) {
        var start = cursor.col();
        var end = cursor.col() + length;
        symbolTable.add(prefix + cursor.line().substring(start, end));

        int index = symbolTable.size() - 1;
        tokens.add(new Token(type, cursor.row(), cursor.col(), index));
        cursor.col(cursor.col() + length);
        state = LexerState.DEFAULT;
    }

    private void addCacheToken(TokenType type, int length) {
        add(type, length, cache.toString());
        cache = new StringBuilder();
    }

    private int read(Predicate<Character> func, int shift) {
        if (cursor.isEnded(shift)) {
            return shift;
        }

        char symbol = cursor.nextChar(shift);

        while (func.test(symbol)) {
            shift++;
            if (cursor.isEnded(shift)) break;

            symbol = cursor.nextChar(shift);
        }

        return shift;
    }

}
