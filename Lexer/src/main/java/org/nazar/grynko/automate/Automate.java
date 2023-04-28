package org.nazar.grynko.automate;

import org.nazar.grynko.TokenType;

public record Automate(AutomateState root) {

    public TokenType getType(String code) {
        var pos = 0;
        var letter = code.charAt(pos);
        var node = root;

        while (node.nextStates().containsKey(letter)) {
            node = node.nextStates().get(letter);
            pos++;

            if (pos >= code.length()) break;
            letter = code.charAt(pos);
        }

        return node.type();
    }

}
