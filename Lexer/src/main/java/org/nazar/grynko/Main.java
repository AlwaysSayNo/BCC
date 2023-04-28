package org.nazar.grynko;

import lombok.SneakyThrows;
import org.nazar.grynko.lexer.Lexer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final String OPERATORS_PATH = "src/main/resources/operators.txt";
    private static final String KEYWORDS_PATH = "src/main/resources/keywords.txt";
    private static final String INPUT_PATH = "src/main/resources/input.swift";

    public static void main(String[] args) {
        List<String> operators = null, keywords = null;
        var filesAreReachable = true;
        String errorMessage = null;

        try {
            operators = readWords(OPERATORS_PATH);
            keywords = readWords(KEYWORDS_PATH);
        } catch (Exception e) {
            filesAreReachable = false;
            errorMessage = e.getMessage();
        }

        if (!filesAreReachable) {
            System.out.println("An error occurred: " + errorMessage);
            return;
        }

        var lexer = new Lexer(operators, keywords);
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

        var sb1 = new StringBuilder();
        lexer.getTokens()
                .forEach(t -> {
                    var val = lexer.getSymbolTable().get(t.getSymbolTableIndex() - 1);
                    sb1.append(String.format(
                            "%d. %s (\"%s\") with length %d", t.getSymbolTableIndex(), t.getType(), val, val.length()));
                    sb1.append(String.format(" on row %d, col %d", t.getRow(), t.getCol()));
                    sb1.append("\n");
                });
        System.out.println(sb1);

        System.out.println("========== INVALID ==========");

        var sb2 = new StringBuilder();
        lexer.getInvalid()
                .forEach(i -> {
                    var val = i.getValue();
                    sb2.append(String.format("(\"%s\") on row %d, col %d", val, i.getRow(), i.getCol()));
                    sb2.append("\n");
                });
        System.out.println(sb2);

    }

}
