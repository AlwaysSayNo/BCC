package org.nazar.grynko;

import lombok.SneakyThrows;
import org.nazar.grynko.lexer.Lexer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {

    private static final String OPERATORS_PATH = "src/main/resources/operators.txt";
    private static final String KEYWORDS_PATH = "src/main/resources/keywords.txt";
    private static final String PUNCTUATIONS_PATH = "src/main/resources/punctuations.txt";
    private static final String INPUT_PATH = "src/main/resources/input.swift";

    public static void main(String[] args) {
        List<String> operators = null, keywords = null, punctuations = null;
        var filesAreReachable = true;
        String errorMessage = null;

        try {
            operators = readWords(OPERATORS_PATH);
            keywords = readWords(KEYWORDS_PATH);
            punctuations = readWords(PUNCTUATIONS_PATH);
        } catch (Exception e) {
            filesAreReachable = false;
            errorMessage = e.getMessage();
        }

        if (!filesAreReachable) {
            System.out.println("An error occurred: " + errorMessage);
            return;
        }

        var lexer = new Lexer(operators, keywords, punctuations);
        lexer.parse(INPUT_PATH);

        print(lexer);
    }

    @SneakyThrows
    private static List<String> readWords(String path) {
        var words = new ArrayList<String>();

        var br = new BufferedReader(new FileReader(path));

        String word;
        while ((word = br.readLine()) != null) {
            words.add(word.trim());
        }

        return words;
    }

    private static void print(Lexer lexer) {
        System.out.println("========== TOKENS ==========");
        printTokens(lexer);

        System.out.println("========== INVALID ==========");
        printInvalid(lexer);
    }

    private static void printTokens(Lexer lexer) {
        if (lexer.getTokens().isEmpty()) {
            System.out.println();
            return;
        }

        var tokens = lexer.getTokens();
        var table = lexer.getSymbolTable();

        var sb = new StringBuilder();
        for (var token: tokens) {
            var value = table.get(token.getSymbolTableIndex());
            sb.append(token.getSymbolTableIndex())
                    .append(". ")
                    .append(token.getType())
                    .append(": '")
                    .append(value)
                    .append("' [")
                    .append(token.getRow())
                    .append(":")
                    .append(token.getCol())
                    .append("] ")
                    .append("length ")
                    .append(value.length())
                    .append("\n");
        }

        System.out.println(sb);
    }

    private static void printInvalid(Lexer lexer) {
        if (lexer.getInvalid().isEmpty()) {
            System.out.println();
            return;
        }

        var sb = new StringBuilder();
        int i = 0;
        for (var invalid: lexer.getInvalid()) {
            sb.append(i)
                    .append(". '")
                    .append(invalid.getValue())
                    .append("'")
                    .append(" [")
                    .append(invalid.getRow())
                    .append(":")
                    .append(invalid.getCol())
                    .append("] ")
                    .append(invalid.getMessage())
                    .append("\n");
        }

        System.out.println(sb);
    }

}
