package org.nazar.grynko.automate;

import org.nazar.grynko.TokenType;
import org.nazar.grynko.TokenPair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AutomateBuilder {

    public static Automate build(List<String> tokenValues) {
        var tokens = new ArrayList<TokenPair>();

        for (var value: tokenValues) {
            var type = TokenType.getByValue(value);
            tokens.add(new TokenPair(type, value));
        }

        var root = new AutomateState();
        build(root, tokens, 0);

        return new Automate(root);
    }

    private static void build (AutomateState root, List<TokenPair> tokens, int depth) {
        var nodes = new HashMap<Character, List<TokenPair>>();

        for (TokenPair token : tokens) {
            if (token.value().length() > depth) {
                var letter = token.value().charAt(depth);

                nodes.putIfAbsent(letter, new ArrayList<>());
                nodes.get(letter).add(token);
            }
        }

        for (var key: nodes.keySet()) {
            var node = new AutomateState();
            root.nextStates().putIfAbsent(key, node);

            for (var token: nodes.get(key)) {
                if (token.value().length() - 1 == depth) {
                    node.type(TokenType.getByValue(token.value()));
                }
            }

            build(node, nodes.get(key), depth + 1);
        }
    }

}
