package chess.calculator;

import chess.ChessPosition;

public abstract class LimitMoveCalculator extends CardinalMoveCalculator {
    @Override
    protected int getLimit(ChessPosition start, boolean flipA, boolean flipB) {
        return 8 - getSpace(start, flipA, flipB);
    }

    @Override
    protected boolean ignoreThird() {
        return true;
    }

    protected abstract int getSpace(ChessPosition start, boolean flipRow, boolean flipCol);
}
