package chess.calculator;

import chess.ChessPosition;

public abstract class OffsetMoveCalculator extends CardinalMoveCalculator {
    @Override
    protected int getLimit(ChessPosition start, boolean flipA, boolean flipB) {
        return 1;
    }

    @Override
    protected boolean ignoreThird() {
        return false;
    }
}
