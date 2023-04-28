package org.nazar.grynko.lexer;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.nazar.grynko.InvalidToken;
import org.nazar.grynko.LineWrapper;
import org.nazar.grynko.Token;
import org.nazar.grynko.TokenType;
import org.nazar.grynko.automate.Automate;
import org.nazar.grynko.automate.AutomateBuilder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import static org.nazar.grynko.Validator.*;

@Getter
@Setter
public class Lexer {

    private final Automate operatorsAutomate;
    private final Automate keywordsAutomate;

    private LexerState state;
    private List<Token> tokens;
    private List<InvalidToken> invalid;
    private LineWrapper lineWrapper;
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
        lineWrapper = new LineWrapper();
        while ((line = reader.readLine()) != null) {
            lineWrapper.line(line)
                    .col(0);

            while (!lineWrapper.isEnded()) {
                processToken();
            }

            lineWrapper.row(lineWrapper.row() + 1);
        }
    }

    private void processToken() {
        var symbol = lineWrapper.nextChar();

        if (isWhitespace(symbol)) {
            processWhitespace();
            return;
        } else if (isTab(symbol)) {
            processTab();
            return;
        }
//        else if (isDigit(symbol)) {
//            processNumber();
//        } else if (isWord(symbol)) {
//            procesWord();
//        } else if (!lineWrapper.isEnded(1)
//                && isCommentOpen(symbol, lineWrapper.nextChar(1))) {
//            processComment();
//        } else if (isDoubleQuote(symbol)) {
//            processDoubleQuote();
//        } else if (isOperator(symbol)) {
//            processOperator();
//        }

        // multiline
        // white space
        // tab
        // digit (int, float)
        // comment
        // identifier
        // keyword + operator
        // dot - can be function or float

        invalid.add(new InvalidToken(symbol, "Invalid symbol", lineWrapper.row(), lineWrapper.col()));
        lineWrapper.col(lineWrapper.col() + 1);
    }

    private void processWhitespace() {
        int shift = 0;
        char space = lineWrapper.nextChar(shift);

        while (space == ' ' && !lineWrapper.isEnded(shift + 1)) {
            shift++;
            space = lineWrapper.nextChar(shift);
        }

        addToken(TokenType.WHITE_SPACE, shift);
    }

    private void processTab() {
        int shift = 0;
        char tab = lineWrapper.nextChar(shift);

        while (tab == '\t' && !lineWrapper.isEnded(shift + 1)) {
            shift++;
            tab = lineWrapper.nextChar(shift);
        }

        addToken(TokenType.TAB, shift);
    }

    private void addCacheToken(TokenType type, int length) {
        add(type, length, cache.toString());
        cache = new StringBuilder();
    }

    private void addToken(TokenType type, int length) {
        add(type, length, "");
    }

    private void add(TokenType type, int length, String prefix) {
        var start = lineWrapper.col();
        var end = lineWrapper.col() + length;
        symbolTable.add(prefix + lineWrapper.line().substring(start, end));

        int index = symbolTable.size() - 1;
        tokens.add(new Token(type, lineWrapper.row(), lineWrapper.col(), index));
        lineWrapper.col(lineWrapper.col() + length);
        state = LexerState.DEFAULT;
    }


}
