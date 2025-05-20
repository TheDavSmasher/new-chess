package chess.calculator;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;
import chess.PieceMoveCalculator;

import java.util.ArrayList;
import java.util.Collection;

public abstract class ProgrammaticMoveCalculator implements PieceMoveCalculator {
    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition start) {
        Collection<ChessMove> endMoves = new ArrayList<>();
        for (boolean flipA : options) {
            for (boolean flipB : options) {
                boolean leaveEarly = false;
                for (boolean flipC : options) {
                    leaveEarly = collectMovesInDirection(board, start, endMoves, flipA, flipB, flipC);
                    if (ignoreThird() || leaveEarly) { break; }
                }
                if (leaveEarly) { break; }
            }
        }
        return endMoves;
    }

    protected abstract boolean collectMovesInDirection(
            ChessBoard board, ChessPosition start, Collection<ChessMove> endMoves,
            boolean flipA, boolean flipB, boolean flipC);
    protected abstract boolean ignoreThird();

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
