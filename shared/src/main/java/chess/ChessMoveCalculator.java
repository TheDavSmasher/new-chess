package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import static chess.ChessGame.*;

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
        for (int i = -1; i < 2; i += 2) {
            temp = new ChessPosition(start.getRow() + pieceDirection, start.getColumn() + i);
            atTemp = board.getPiece(temp);
            if (atTemp != null && atTemp.color() != color) {
                addPawnPromotionMoves(endMoves, start, temp, color);
            }
        }
        return endMoves;
    }

    private static void addPawnPromotionMoves(Collection<ChessMove> moves, ChessPosition start, ChessPosition end, ChessGame.TeamColor color) {
        if (end.getRow() == getTeamInitialRow(getOtherTeam(color))) {
            Collections.addAll(moves, new ChessMove(start, end, ChessPiece.PieceType.QUEEN), new ChessMove(start, end, ChessPiece.PieceType.ROOK), new ChessMove(start, end, ChessPiece.PieceType.BISHOP), new ChessMove(start, end, ChessPiece.PieceType.KNIGHT));
        } else {
            moves.add(new ChessMove(start, end, null));
        }
    }

    public static Collection<ChessMove> getCross(ChessBoard board, ChessPosition start) {
        int[] limits = {
                start.getColumn(),
                9 - start.getColumn(),
                start.getRow(),
                9 - start.getRow()
        };
        int[] modifiers = { 0, 0, +1, -1 };
        Collection<ChessMove> endMoves = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            endMoves.addAll(getMovesFromLimits(board, start, limits[i], modifiers[i], modifiers[(i + 2) % 4]));
        }
        return endMoves;
    }

    public static Collection<ChessMove> getDiagonals(ChessBoard board, ChessPosition start) {
        int[] limits = {
                Math.max(start.getRow(), start.getColumn()),
                Math.max(start.getRow(), 9 - start.getColumn()),
                Math.max(9 - start.getRow(), start.getColumn()),
                Math.max(9 - start.getRow(), 9 - start.getColumn())
        };
        int[] modifiers = { +1, -1 };
        Collection<ChessMove> endMoves = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            endMoves.addAll(getMovesFromLimits(board, start, limits[i], modifiers[Math.floorDiv(i, 2)], modifiers[i % 2]));
        }
        return endMoves;
    }

    public static Collection<ChessMove> getKing(ChessBoard board, ChessPosition start) {
        int[][] offsets = { {0, +1}, {0, -1}, {+1, 0}, {-1, 0}, {+1, +1}, {+1, -1}, {-1, -1}, {-1, +1} };
        return getMovesFromOffsets(board, start, offsets);
    }

    public static Collection<ChessMove> getKnight(ChessBoard board, ChessPosition start) {
        int[][] offsets = { {+2, +1}, {+1, +2}, {-2, +1}, {-1, +2}, {-2, -1}, {-1, -2}, {+2, -1}, {+1, -2} };
        return getMovesFromOffsets(board, start, offsets);
    }

    private static Collection<ChessMove> getMovesFromLimits(ChessBoard board, ChessPosition start, int spaces, int rowMod, int colMod) {
        int limit = 8 - spaces;
        int[][] offsets = new int[limit][];
        for (int i = 1; i <= limit; i++) {
            offsets[i - 1] = new int[] { i * rowMod, i * colMod };
        }
        return getMovesFromOffsets(board, start, offsets, false);
    }

    private static Collection<ChessMove> getMovesFromOffsets(ChessBoard board, ChessPosition start, int[][] offsets) {
        return getMovesFromOffsets(board, start, offsets, true);
    }

    private static Collection<ChessMove> getMovesFromOffsets(ChessBoard board, ChessPosition start, int[][] offsets, boolean checkBounds) {
        Collection<ChessMove> endMoves = new ArrayList<>();
        ChessGame.TeamColor color = board.getPiece(start).color();
        for (int[] offset : offsets) {
            ChessPosition temp = new ChessPosition(start.getRow() + offset[0], start.getColumn() + offset[1]);
            if (checkBounds && temp.outOfBounds()) continue;
            ChessPiece atTemp = board.getPiece(temp);
            if (atTemp == null || (atTemp.color() != color)) {
                endMoves.add(new ChessMove(start, temp, null));
            }
            if (!checkBounds && atTemp != null) { break; }
        }
        return endMoves;
    }
}
