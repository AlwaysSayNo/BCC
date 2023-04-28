package org.nazar.grynko.automate;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.nazar.grynko.TokenType;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Accessors(chain = true, fluent = true)
public class AutomateState {

    private TokenType type = TokenType.INVALID;
    private Map<Character, AutomateState> nextStates = new HashMap<>();

}
