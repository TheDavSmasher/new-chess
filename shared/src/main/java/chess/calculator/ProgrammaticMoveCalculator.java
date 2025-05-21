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

    protected boolean collectMovesInDirection(
            ChessBoard board, ChessPosition start, Collection<ChessMove> endMoves,
            boolean flipA, boolean flipB, boolean flipC) {
        for (int i = 1; i <= getLimit(start, flipA, flipB); i++) {
            ChessPosition temp = new ChessPosition(
                    start.row() + i * getDirMod(true, flipA, flipB, flipC),
                    start.col() + i * getDirMod(false, flipA, flipB, flipC));
            if (temp.outOfBounds()) continue;
            Boolean state = checkAndAdd(endMoves, board, start, temp, flipA);
            if (state == null) {
                break;
            } else if (state) {
                return true;
            }
        }
        return false;
    }

    protected abstract Boolean checkAndAdd(Collection<ChessMove> endMoves, ChessBoard board, ChessPosition start, ChessPosition temp, boolean flipA);
    protected abstract int getLimit(ChessPosition start, boolean flipA, boolean flipB);
    protected abstract int getDirMod(boolean isRow, boolean flipA, boolean flipB, boolean flipC);
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
