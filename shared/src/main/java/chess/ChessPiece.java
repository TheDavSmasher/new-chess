package chess;

import chess.calculator.*;

import java.util.Collection;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public record ChessPiece(ChessGame.TeamColor color, PieceType type) {
    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        return (switch (type) {
            case BISHOP -> new DiagonalMoveCalculator();
            case ROOK -> new CrossMoveCalculator();
            case QUEEN -> new QueenMoveCalculator();
            case KING -> new KingMoveCalculator();
            case KNIGHT -> new KnightMoveCalculator();
            case PAWN -> new PawnMoveCalculator();
        }).calculateMoves(board, myPosition);
    }

    @Override
    public String toString() {
        String s = switch (type) {
            case BISHOP -> "b";
            case KNIGHT -> "n";
            case ROOK -> "r";
            case KING -> "k";
            case PAWN -> "p";
            case QUEEN ->"q";
        };
        return color == ChessGame.TeamColor.WHITE ? s.toUpperCase() : s;
    }
}
