package chess;

import java.util.Arrays;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard implements Cloneable {
    public static final int BOARD_SIZE = 8;
    private ChessPiece[][] board = new ChessPiece[BOARD_SIZE][BOARD_SIZE];

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        board[position.getRow() - 1][position.getColumn() - 1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        if (position.outOfBounds()) return null;
        return board[position.getRow() - 1][position.getColumn() - 1];
    }

    private static final ChessPiece.PieceType[] PIECE_ORDER = {
        ChessPiece.PieceType.ROOK, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.QUEEN,
        ChessPiece.PieceType.KING, ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.ROOK
    };

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        board = new ChessPiece[BOARD_SIZE][BOARD_SIZE];

        for (var team : ChessGame.TeamColor.values()) {
            int row = ChessGame.getTeamInitialRow(team) - 1;
            int dir = ChessGame.getTeamDirection(team);

            for (int i = 0; i < BOARD_SIZE; i++) {
                board[row][i] = new ChessPiece(team, PIECE_ORDER[i]);
                board[row + dir][i] = new ChessPiece(team, ChessPiece.PieceType.PAWN);
            }
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("  1 2 3 4 5 6 7 8\n");
        for (int i = 0; i < BOARD_SIZE; i++) {
            builder.append(i + 1);
            for (int j = 0; j < BOARD_SIZE; j++) {
                builder.append('|').append(board[i][j] != null ? board[i][j] : " ");
            }
            builder.append('\n');
        }
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessBoard that = (ChessBoard) o;
        return Arrays.deepEquals(board, that.board);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }

    @Override
    public ChessBoard clone() {
        try {
            ChessBoard clone = (ChessBoard) super.clone();
            clone.board = Arrays.copyOf(board, board.length);
            for (int i = 0; i < BOARD_SIZE; i++) {
                clone.board[i] = Arrays.copyOf(board[i], board[i].length);
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}