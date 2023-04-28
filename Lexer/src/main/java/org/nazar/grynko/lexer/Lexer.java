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
        }
//        else if (isWord(symbol)) {
//            procesWord();
//            return;
//        }
//        else if (!lineCursor.isEnded(1)
//                && isCommentOpen(symbol, lineCursor.nextChar(1))) {
//            processComment();
//        } else if (isDoubleQuote(symbol)) {
//            processDoubleQuote();
//        } else if (isOperator(symbol)) {
//            processOperator();
//        }

        // multiline
        // digit (int, float)
        // comment
        // identifier
        // keyword + operator
        // dot - can be function or float

        processBadToken(0);
    }

    private void processWhitespace() {
        int shift = read(Validator::isWhitespace, 0);
        addToken(TokenType.WHITE_SPACE, shift);
    }

    private void processTab() {
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

    private void processBadToken(int shift) {
        state = LexerState.ERROR;
        int row = cursor.row(), col = cursor.col();

        if (cursor.isEnded(shift + 1)) {
            shift++;
        } else {
            shift = read((Character c) -> !isEndOfToken(c), shift);
        }

        addInvalid(cursor.line().substring(col, col + shift), "Invalid token", row, col);

        cursor.col(col + shift);
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

    private void addInvalid(Character symbol, String message, int row, int col) {
        addInvalid(symbol.toString(), message, row, col);
    }

    private void addInvalid(String token, String message, int row, int col) {
        state = LexerState.DEFAULT;
        invalid.add(new InvalidToken(token, message, row, col));
        cursor.col(cursor.col() + 1);
    }

    private int read(Predicate<Character> func, int shift) {
        char symbol = cursor.nextChar(shift);

        while (func.test(symbol) && !cursor.isEnded(shift + 1)) {
            shift++;
            symbol = cursor.nextChar(shift);
        }

        return shift;
    }

}
