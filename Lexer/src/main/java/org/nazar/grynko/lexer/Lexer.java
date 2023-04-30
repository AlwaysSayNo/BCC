package org.nazar.grynko.lexer;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.nazar.grynko.*;
import org.nazar.grynko.automate.Automate;
import org.nazar.grynko.automate.AutomateBuilder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.function.Predicate;

import static org.nazar.grynko.Validator.*;
import static org.nazar.grynko.lexer.LexerState.*;

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
    private Stack<LexerCache> cache;

    public Lexer(List<String> operators, List<String> keywords) {
        operatorsAutomate = AutomateBuilder.build(operators);
        keywordsAutomate = AutomateBuilder.build(keywords);

        state = LexerState.DEFAULT;
        tokens = new ArrayList<>();
        invalid = new ArrayList<>();
        symbolTable = new ArrayList<>();
        cache = new Stack<>();
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

        if (state != DEFAULT) {
            errorAfterParse();
        }
    }

    private void errorAfterParse() {
        if (!cache.isEmpty()) {
            processCacheErrorAfterParse();
        }
    }

    private void processCacheErrorAfterParse() {
        while(!cache.isEmpty()) {
            var lexerCache = cache.pop();
            int row = lexerCache.getRow(), col = lexerCache.getCol();

            if (lexerCache.getState() == MULTILINE_COMMENT) {
                var message = String.format("Multiline comment on [%d:%d] wasn't closed", row, col);
                processBadToken(message, 0);
            }
        }
    }

    private void processToken() {
        if (state != DEFAULT) {
            processNonDefaultMode();
            return;
        }

        var symbol = cursor.nextChar();

        if (isWhitespace(symbol)) {
            processWhitespace();
            return;
        } else if (isTab(symbol)) {
            processTab();
            return;
        } else if (!cursor.isEnded(1) && isSingleLineCommentOpen(symbol, cursor.nextChar(1))) {
            processSingleLineComment();
            return;
        } else if (!cursor.isEnded(1) && isMultilineCommentOpen(symbol, cursor.nextChar(1))) {
            processMultilineComment();
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
        // punctuation
        // dot - can be function or float
        // interpolation
        // """ """

        processBadToken(cursor.nextChar());
    }

    private void processNonDefaultMode() {
        if (state == MULTILINE_COMMENT) {
            processMultilineComment();
        }
        else {
            processBadToken(0);
        }
    }

    private void processWhitespace() {
        state = WHITE_SPACE;

        int shift = read(Validator::isWhitespace, 0);
        addToken(TokenType.WHITE_SPACE, shift);
    }

    private void processTab() {
        state = TAB;

        int shift = read(Validator::isTab, 0);
        addToken(TokenType.TAB, shift);
    }

    private void processNumber() {
        state = INTEGER;

        int shift = read(Validator::isDigit, 0);

        if (!cursor.isEnded(shift) && isDot(cursor.nextChar(shift))) {
            state = FLOAT;
            shift++;
        }

        if (!cursor.isEnded(shift)) {
            shift = read(Validator::isDigit, shift);
        }

        if (cursor.isEnded(shift) || isEndOfToken(cursor.nextChar(shift))) {
            var tokenType = state == INTEGER ? TokenType.INT : TokenType.FLOAT;
            addToken(tokenType, shift);
        } else {
            processBadToken(shift);
        }
    }

    private void processWord() {
        state = IDENTIFIER;

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
                state = OPERATOR;
            }
        } else {
            type = keywordsAutomate.getType(word);
            if (type != TokenType.INVALID) {
                state = KEYWORD;
            }
        }

        if (type == TokenType.INVALID) {
            addToken(TokenType.IDENTIFIER, shift);
        } else {
            addToken(type, shift);
        }
    }

    private void processOperator() {
        state = OPERATOR;

        int shift = read(Validator::isOperator, 0);
        var col = cursor.col();
        var word = cursor.line().substring(col, col + shift);

        var type = operatorsAutomate.getType(word);
        if (type == TokenType.INVALID) {
            processBadToken(shift);
        } else {
            addToken(type, shift);
        }
    }

    private void processDoubleQuote() {
        state = STRING;

        int shift = 0;
        char symbol;

        while (!cursor.isEnded(shift + 1)) {
            shift++;
            symbol = cursor.nextChar(shift);

            if (state == STRING) {
                if (symbol == '\\') {
                    state = STRING_SLASH;
                } else if (symbol == '\"') {
                    shift++;
                    addToken(TokenType.STRING_LITERAL, shift);
                    return;
                }
            } else if (state == STRING_SLASH) {
                state = STRING;
            }
        }

        var message = String.format(
                "No closing quote was found for the first quote \" [%d:%d]", cursor.row(), cursor.col());
        processBadToken(message, shift);
    }

    private void processTypeDeclaration() {
        state = TYPE_DECLARATION;

        int shift = read(Validator::isWord, 1);

        // we read only @ sign - incorrect token
        if (shift == 1) {
            processBadToken(shift);
            return;
        }

        addToken(TokenType.TYPE_DECLARATION, shift);
    }

    private void processSingleLineComment() {
        state = SINGLE_LINE_COMMENT;
        addToken(TokenType.SINGLE_LINE_COMMENT, cursor.line().length() - cursor.col());
    }

    private void processMultilineComment() {
        state = MULTILINE_COMMENT;
        int shift;
        char symbol;

        if (isMultilineCommentOpen(cursor.nextChar(), cursor.nextChar(1))) {
            var sb = new StringBuilder();
            int row = cursor.row(), col = cursor.col();

            var lexerCache = new LexerCache(MULTILINE_COMMENT, sb, row, col);
            cache.push(lexerCache);

            shift = 1;
        }
        else {
            shift = 0;
            if (cursor.nextChar() == '*') {
                state = MULTILINE_COMMENT_ENDING;
            }
        }


        while (!cursor.isEnded(shift + 1)) {
            shift++;
            symbol = cursor.nextChar(shift);

            if (state == MULTILINE_COMMENT) {
                if (symbol == '*') {
                    state = MULTILINE_COMMENT_ENDING;
                }
            } else if (state == MULTILINE_COMMENT_ENDING) {
                if (symbol == '/') {
                    shift++;
                    addCacheToken(TokenType.MULTILINE_COMMENT, shift);
                    return;
                }
                else if (symbol != '*') {
                    state = MULTILINE_COMMENT;
                }
            }
        }

        int col = cursor.col();
        cache.peek().getData().append(cursor.line(), col, shift + 1).append("\n");

        cursor.col(col + shift + 1);
        state = MULTILINE_COMMENT;
    }

    private void processBadToken(Character c) {
        addInvalid(c.toString(), "Invalid character", 1);
    }

    private void processBadToken(int shift) {
        processBadToken("Invalid token", shift);
    }

    private void processBadToken(String message, int shift) {
        int col = cursor.col();
        var states = List.of(OPERATOR);

        if (shift == 0 && cursor.line().length() == col + 1) {
            shift++;
        } else if (!cursor.isEnded(shift) && !states.contains(state)) {
            shift = read((Character c) -> !isEndOfToken(c), shift);
        }

        addInvalid(cursor.line().substring(col, col + shift), message, shift);
    }

    private void addInvalid(String token, String message, int shift) {
        int row = cursor.row(), col = cursor.col();
        invalid.add(new InvalidToken(token, message, row, col));
        cursor.col(col + shift);

        state = DEFAULT;
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

        state = DEFAULT;
    }

    private void addCacheToken(TokenType type, int length) {
        var lexerCache = cache.pop();
        add(type, length, lexerCache.getData().toString());

        if (!cache.isEmpty()) {
            state = cache.peek().getState();
        }
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
