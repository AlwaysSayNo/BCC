package org.nazar.grynko;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InvalidToken {

    private String value;
    private String message;
    private int row;
    private int col;

}
