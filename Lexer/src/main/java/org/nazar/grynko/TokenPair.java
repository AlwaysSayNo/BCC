package org.nazar.grynko;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@AllArgsConstructor
@Accessors(chain = true, fluent = true)
public class TokenPair {

    private TokenType type;
    private String value;

}
