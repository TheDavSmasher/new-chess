package chess.calculator;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;
import chess.PieceMoveCalculator;

import java.util.Collection;

public class QueenMoveCalculator implements PieceMoveCalculator {
    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition start) {
        Collection<ChessMove> endMoves = new CrossMoveCalculator().calculateMoves(board, start);
        endMoves.addAll(new DiagonalMoveCalculator().calculateMoves(board, start));
        return endMoves;
    }
}
