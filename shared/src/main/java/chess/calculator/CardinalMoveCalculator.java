package chess.calculator;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;

public abstract class CardinalMoveCalculator extends PieceMoveCalculator {
    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition start) {
        Collection<ChessMove> endMoves = new ArrayList<>();
        for (boolean flipA : options) {
            for (boolean flipB : options) {
                for (boolean flipC : options) {
                    endMoves.addAll(getMovesFromLimits(board, start, getLimit(start, flipA, flipB),
                            getDirMod(true, flipA, flipB, flipC), getDirMod(false, flipA, flipB, flipC)));
                    if (ignoreThird()) break;
                }
            }
        }
        return endMoves;
    }

    protected abstract int getLimit(ChessPosition start, boolean flipA, boolean flipB);
    protected abstract int getDirMod(boolean isRow, boolean flipA, boolean flipB, boolean flipC);
    protected abstract boolean ignoreThird();

    private static Collection<ChessMove> getMovesFromLimits(ChessBoard board, ChessPosition start, int limit, int rowMod, int colMod) {
        int[][] offsets = new int[limit][];
        for (int i = 1; i <= limit; i++) {
            offsets[i - 1] = new int[] { i * rowMod, i * colMod };
        }
        return getMovesFromOffsets(board, start, offsets);
    }

    private static Collection<ChessMove> getMovesFromOffsets(ChessBoard board, ChessPosition start, int[][] offsets) {
        Collection<ChessMove> endMoves = new ArrayList<>();
        ChessGame.TeamColor color = board.getPiece(start).color();
        for (int[] offset : offsets) {
            ChessPosition temp = new ChessPosition(start.getRow() + offset[0], start.getColumn() + offset[1]);
            if (temp.outOfBounds()) continue;
            ChessPiece atTemp = board.getPiece(temp);
            if (atTemp == null || (atTemp.color() != color)) {
                endMoves.add(new ChessMove(start, temp, null));
            }
            if (atTemp != null) { break; }
        }
        return endMoves;
    }
}
