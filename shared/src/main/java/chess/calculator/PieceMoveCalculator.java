package chess.calculator;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;

public abstract class PieceMoveCalculator {
    public abstract Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition start);

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
