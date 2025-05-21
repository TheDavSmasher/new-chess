package chess.calculator;

import chess.*;

import java.util.Collection;

public abstract class CardinalMoveCalculator extends ProgrammaticMoveCalculator {
    @Override
    protected Boolean checkAndAdd(Collection<ChessMove> endMoves, ChessBoard board,
                                  ChessPosition start, ChessPosition temp, boolean ignored) {
        ChessPiece atTemp = board.getPiece(temp);
        if (atTemp == null || (atTemp.color() != board.getPiece(start).color())) {
            endMoves.add(new ChessMove(start, temp));
        }
        if (atTemp != null) { return null; }
        return false;
    }
}
