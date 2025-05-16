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
                    int limit = getLimit(start, flipA, flipB);
                    int rowMod = getDirMod(true, flipA, flipB, flipC);
                    int colMod = getDirMod(false, flipA, flipB, flipC);

                    for (int i = 1; i <= limit; i++) {
                        ChessPosition temp = start.offsetBy(i * rowMod, i * colMod);
                        if (temp.outOfBounds()) continue;
                        ChessPiece atTemp = board.getPiece(temp);
                        if (atTemp == null || (atTemp.color() != board.getPiece(start).color())) {
                            endMoves.add(new ChessMove(start, temp));
                        }
                        if (atTemp != null) { break; }
                    }
                    if (ignoreThird()) break;
                }
            }
        }
        return endMoves;
    }

    protected abstract int getLimit(ChessPosition start, boolean flipA, boolean flipB);
    protected abstract int getDirMod(boolean isRow, boolean flipA, boolean flipB, boolean flipC);
    protected abstract boolean ignoreThird();
}
