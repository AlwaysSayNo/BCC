package org.nazar.grynko;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(fluent = true)
public class LineCursor {

    private String line;
    private int row;
    private int col;

    public Character nextChar() {
        return line.charAt(col);
    }

    public Character nextChar(int shift) {
        return line.charAt(col + shift);
    }

    public boolean isEnded() {
        return col >= line.length();
    }

    public boolean isEnded(int shift) {
        return col + shift >= line.length();
    }

}
