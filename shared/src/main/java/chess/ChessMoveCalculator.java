package chess;

import java.util.ArrayList;
import java.util.Collection;
import static chess.ChessGame.*;

@FunctionalInterface
interface BoolToInt {
    int apply(boolean i);
}

public class ChessMoveCalculator {
    public static Collection<ChessMove> getQueen(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> queenMoves = getDiagonals(board, myPosition);
        queenMoves.addAll(getCross(board, myPosition));
        return queenMoves;
    }

    public static Collection<ChessMove> getPawn(ChessBoard board, ChessPosition start) { //Fail: 10, 11
        Collection<ChessMove> endMoves = new ArrayList<>();
        ChessGame.TeamColor color = board.getPiece(start).color();
        int pieceDirection = getTeamDirection(color);
        ChessPiece atTemp;
        //Move Forward
        ChessPosition temp = new ChessPosition(start.getRow() + pieceDirection, start.getColumn());

        atTemp = board.getPiece(temp);
        if (atTemp == null) {
            addPawnPromotionMoves(endMoves, start, temp, color);

            //Special Case: Initial Move up to 2 forward
            temp = new ChessPosition(start.getRow() + (2 * pieceDirection), start.getColumn());
            if (start.getRow() == (color == ChessGame.TeamColor.BLACK ? 7 : 2) && board.getPiece(temp) == null) {
                endMoves.add(new ChessMove(start, temp, null));
            }
        }

        //Eating
        for (boolean dir : options) {
            temp = new ChessPosition(start.getRow() + pieceDirection, start.getColumn() + getMod(dir));
            atTemp = board.getPiece(temp);
            if (atTemp != null && atTemp.color() != color) {
                addPawnPromotionMoves(endMoves, start, temp, color);
            }
        }
        return endMoves;
    }

    private static final ChessPiece.PieceType[] promotions = {
        ChessPiece.PieceType.QUEEN, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.ROOK
    };

    private static void addPawnPromotionMoves(Collection<ChessMove> moves, ChessPosition start, ChessPosition end, ChessGame.TeamColor color) {
        ChessPiece.PieceType[] pieces = end.getRow() == getTeamInitialRow(getOtherTeam(color))
            ? promotions
            : new ChessPiece.PieceType[] {null};
        for (var pieceType : pieces) {
            moves.add(new ChessMove(start, end, pieceType));
        }
    }

    private static final boolean[] options = { false, true };

    private static int getMod(boolean b) {
        return getMod(false, b);
    }

    private static int getMod(boolean a, boolean b) {
        return a ? 0 : b ? -1 : 1;
    }

    private static int mirrorIf(int value, boolean invert) {
        return invert ? 9 - value : value;
    }

    public static Collection<ChessMove> getCross(ChessBoard board, ChessPosition start) {
        Collection<ChessMove> endMoves = new ArrayList<>();
        for (boolean row : options) {
            for (boolean col : options) {
                endMoves.addAll(getMovesFromLimits(board, start,
                    8 - mirrorIf(row ? start.getRow() : start.getColumn(), col),
                getMod(!row, col), getMod(row, col)));
            }
        }
        return endMoves;
    }

    public static Collection<ChessMove> getDiagonals(ChessBoard board, ChessPosition start) {
        Collection<ChessMove> endMoves = new ArrayList<>();
        for (boolean row : options) {
            for (boolean col : options) {
                endMoves.addAll(getMovesFromLimits(board, start,
                    8 - Math.max(mirrorIf(start.getRow(), row), mirrorIf(start.col(), col)),
                getMod(row), getMod(col)));
            }
        }
        return endMoves;
    }

    public static Collection<ChessMove> getKing(ChessBoard board, ChessPosition start) {
        Collection<ChessMove> endMoves = new ArrayList<>();
        for (boolean half : options) {
            for (boolean quarter : options) {
                for (boolean slice : options) {
                    BoolToInt f = b -> getMod(slice && b, quarter && b) * getMod(half);
                    endMoves.addAll(getMovesFromLimits(board, start, f.apply(quarter), f.apply(!quarter)));
                }
            }
        }
        return endMoves;
    }

    public static Collection<ChessMove> getKnight(ChessBoard board, ChessPosition start) {
        Collection<ChessMove> endMoves = new ArrayList<>();
        BoolToInt f = d -> d ? 1 : 2;
        for (boolean row : options) {
            for (boolean col : options) {
                for (boolean shape : options) {
                    endMoves.addAll(getMovesFromLimits(board, start,
                            f.apply(shape) * getMod(row), f.apply(!shape) * getMod(col)));
                }
            }
        }
        return endMoves;

    }

    private static Collection<ChessMove> getMovesFromLimits(ChessBoard board, ChessPosition start, int rowMod, int colMod) {
        return getMovesFromLimits(board, start, 1, rowMod, colMod);
    }

    private static Collection<ChessMove> getMovesFromLimits(ChessBoard board, ChessPosition start, int limit, int rowMod, int colMod) {
        int[][] offsets = new int[limit][];
        for (int i = 1; i <= limit; i++) {
            offsets[i - 1] = new int[] { i * rowMod, i * colMod };
        }

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
