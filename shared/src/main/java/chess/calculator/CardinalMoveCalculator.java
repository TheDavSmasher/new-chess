package chess.calculator;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;

public abstract class CardinalMoveCalculator extends ProgrammaticMoveCalculator {
    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition start) {
        Collection<ChessMove> endMoves = new ArrayList<>();
        for (boolean flipA : options) {
            for (boolean flipB : options) {
                for (boolean flipC : options) {
                    collectMovesInDirection(board, start, endMoves, flipA, flipB, flipC);
                    if (ignoreThird()) { break; }
                }
            }
        }
        return endMoves;
    }

    private void collectMovesInDirection(ChessBoard board, ChessPosition start, Collection<ChessMove> endMoves,
                                         boolean flipA, boolean flipB, boolean flipC) {
        for (int i = 1; i <= getLimit(start, flipA, flipB); i++) {
            ChessPosition temp = new ChessPosition(
                    start.row() + i * getDirMod(true, flipA, flipB, flipC),
                    start.col() + i * getDirMod(false, flipA, flipB, flipC));
            if (temp.outOfBounds()) continue;
            ChessPiece atTemp = board.getPiece(temp);
            if (atTemp == null || (atTemp.color() != board.getPiece(start).color())) {
                endMoves.add(new ChessMove(start, temp));
            }
            if (atTemp != null) { break; }
        }
    }

    protected abstract int getLimit(ChessPosition start, boolean flipA, boolean flipB);
    protected abstract int getDirMod(boolean isRow, boolean flipA, boolean flipB, boolean flipC);
    protected abstract boolean ignoreThird();
}
