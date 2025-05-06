package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class ChessMoveCalculator {
    public static Collection<ChessMove> getQueen(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> queenMoves = getDiagonals(board, myPosition);
        queenMoves.addAll(getCross(board, myPosition));
        return queenMoves;
    }

    public static Collection<ChessMove> getPawn(ChessBoard board, ChessPosition start) { //Fail: 10, 11
        Collection<ChessMove> endMoves = new ArrayList<>();
        ChessGame.TeamColor color = board.getPiece(start).color();
        int pieceDirection = (color == ChessGame.TeamColor.BLACK ? -1 : 1);
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
        if (end.getRow() == (color == ChessGame.TeamColor.BLACK ? 1 : 8)) {
            Collections.addAll(moves, new ChessMove(start, end, ChessPiece.PieceType.QUEEN), new ChessMove(start, end, ChessPiece.PieceType.ROOK), new ChessMove(start, end, ChessPiece.PieceType.BISHOP), new ChessMove(start, end, ChessPiece.PieceType.KNIGHT));
        } else {
            moves.add(new ChessMove(start, end, null));
        }
    }

    public static Collection<ChessMove> getCross(ChessBoard board, ChessPosition start) {
        int[] limits = {
                8 - start.getColumn(),
                start.getColumn() - 1,
                8 - start.getRow(),
                start.getRow() - 1
        };
        int[] modifiers = { 0, 0, +1, -1 };
        Collection<ChessMove> endMoves = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            endMoves.addAll(getMovesFromList(board, start, limits[i], modifiers[i], modifiers[(i + 2) % 4]));
        }
        return endMoves;
    }

    public static Collection<ChessMove> getDiagonals(ChessBoard board, ChessPosition start) {
        int[] limits = {
                8 - Math.max(start.getRow(), start.getColumn()),
                8 - Math.max(start.getRow(), 9 - start.getColumn()),
                8 - Math.max(9 - start.getRow(), start.getColumn()),
                8 - Math.max(9 - start.getRow(), 9 - start.getColumn())
        };
        Collection<ChessMove> endMoves = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            endMoves.addAll(getMovesFromList(board, start, limits[i], (i < 2 ? +1 : -1), (i % 2 == 0 ? +1 : -1)));
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

    private static Collection<ChessMove> getMovesFromList(ChessBoard board, ChessPosition start, int limit, int rowMod, int colMod) {
        Collection<ChessMove> endMoves = new ArrayList<>();
        ChessGame.TeamColor color = board.getPiece(start).color();
        for (int i = 1; i <= limit; i++) {
            ChessPosition temp = new ChessPosition(start.getRow() + (i * rowMod), start.getColumn() + (i * colMod));
            ChessPiece atTemp = board.getPiece(temp);
            if (atTemp == null || (atTemp.color() != color)) {
                endMoves.add(new ChessMove(start, temp, null));
            }
            if (atTemp != null) { break; }
        }
        return endMoves;
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
        }
        return endMoves;
    }
}
