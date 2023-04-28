package org.nazar.grynko.automate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.nazar.grynko.TokenType;

public record Automate(AutomateState root) {

    public TokenPosition getValue(String code, int pos) {
        return getValue(root, code, pos);
    }

    private TokenPosition getValue(AutomateState node, String code, int pos) {
        if (code.length() <= pos) {
            return new TokenPosition(node.type(), pos);
        }

        char letter = code.charAt(pos);
        if (node.nextStates().containsKey(letter)) {
            var nextNode = node.nextStates().get(letter);
            return getValue(nextNode, code, pos + 1);
        }

        return new TokenPosition(node.type(), pos);
    }

    @Getter
    @Setter
    @Accessors(fluent = true)
    @AllArgsConstructor
    class TokenPosition {
        private TokenType type;
        private int position;
    }

}
