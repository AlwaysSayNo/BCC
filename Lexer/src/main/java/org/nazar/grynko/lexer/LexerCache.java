package org.nazar.grynko.lexer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LexerCache {

    private LexerState state;
    private StringBuilder data;
    private int row;
    private int col;

}
