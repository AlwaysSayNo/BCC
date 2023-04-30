package org.nazar.grynko.automate;

import org.nazar.grynko.TokenType;

public record Automate(AutomateState root) {

    public TokenType getType(String code) {
        var pos = 0;
        var letter = code.charAt(pos);
        var length = code.length();
        var node = root;

        while (node.nextStates().containsKey(letter)) {
            node = node.nextStates().get(letter);
            pos++;

            if (pos >= length) break;
            letter = code.charAt(pos);
        }

        return pos >= length ? node.type() : TokenType.INVALID;
    }

}
