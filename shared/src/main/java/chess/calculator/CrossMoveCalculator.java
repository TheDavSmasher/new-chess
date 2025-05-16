package chess.calculator;

import chess.ChessPosition;

public class CrossMoveCalculator extends LimitMoveCalculator {
    @Override
    protected int getLimit(ChessPosition start, boolean flipRow, boolean flipCol) {
        return mirrorIf(flipRow ? start.getRow() : start.getColumn(), flipCol);
    }

    @Override
    protected int getDirMod(boolean isRow, boolean flipRow, boolean flipCol) {
        return getMod(flipRow ^ isRow, flipCol);
    }
}
