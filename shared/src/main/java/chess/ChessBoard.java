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
    private ChessPiece[][] board;

    public ChessBoard() {
         board = new ChessPiece[BOARD_SIZE][BOARD_SIZE];
    }

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

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        board = new ChessPiece[BOARD_SIZE][BOARD_SIZE];
        for (var team : ChessGame.TeamColor.values()) {
            int row = ChessGame.getTeamInitialRow(team) - 1;
            
            board[row][0] = new ChessPiece(team, ChessPiece.PieceType.ROOK);
            board[row][1] = new ChessPiece(team, ChessPiece.PieceType.KNIGHT);
            board[row][2] = new ChessPiece(team, ChessPiece.PieceType.BISHOP);
            board[row][3] = new ChessPiece(team, ChessPiece.PieceType.QUEEN);
            board[row][4] = new ChessPiece(team, ChessPiece.PieceType.KING);
            board[row][5] = new ChessPiece(team, ChessPiece.PieceType.BISHOP);
            board[row][6] = new ChessPiece(team, ChessPiece.PieceType.KNIGHT);
            board[row][7] = new ChessPiece(team, ChessPiece.PieceType.ROOK);

            int second = row + ChessGame.getTeamDirection(team);
            for (int i = 0; i < BOARD_SIZE; i++) {
                board[second][i] = new ChessPiece(team, ChessPiece.PieceType.PAWN);
            }
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("  1 2 3 4 5 6 7 8\n");
        for (int i = 0; i < BOARD_SIZE; i++) {
            builder.append(i + 1);
            for (int j = 0; j < BOARD_SIZE; j++) {
                builder.append('|');
                builder.append(board[i][j] != null ? board[i][j] : " ");
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