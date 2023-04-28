package org.nazar.grynko;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Token {

    private TokenType type;
    private int row;
    private int col;
    private int symbolTableIndex;

}
