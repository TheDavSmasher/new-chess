package chess.calculator;

import chess.PieceMoveCalculator;

public abstract class ProgrammaticMoveCalculator implements PieceMoveCalculator {
    protected static boolean[] options = { false, true };

    protected static int getOffset(boolean a) {
        return a ? 2 : 1;
    }

    protected static int getMod(boolean b) {
        return getMod(false, b);
    }

    protected static int getMod(boolean a, boolean b) {
        return a ? 0 : b ? -1 : 1;
    }

    protected static int mirrorIf(int value, boolean invert) {
        return invert ? 9 - value : value;
    }
}
