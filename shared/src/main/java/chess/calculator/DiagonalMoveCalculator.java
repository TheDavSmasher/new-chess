package chess.calculator;

import chess.ChessPosition;

public class DiagonalMoveCalculator extends LimitMoveCalculator {
    @Override
    protected int getSpace(ChessPosition start, boolean flipRow, boolean flipCol) {
        return Math.max(mirrorIf(start.getRow(), flipRow), mirrorIf(start.getColumn(), flipCol));
    }

    @Override
    protected int getDirMod(boolean isRow, boolean flipRow, boolean flipCol) {
        return getMod(isRow ? flipRow : flipCol);
    }
}
