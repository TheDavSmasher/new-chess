package chess;

import java.util.Collection;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private final ChessGame.TeamColor color;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
        color = pieceColor;
        this.type = type;
    }

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
        return switch (type) {
            case BISHOP -> ChessMoveCalculator.getDiagonals(board, myPosition);
            case ROOK -> ChessMoveCalculator.getCross(board, myPosition);
            case QUEEN -> ChessMoveCalculator.getQueen(board, myPosition);
            case KING -> ChessMoveCalculator.getKing(board, myPosition);
            case KNIGHT -> ChessMoveCalculator.getKnight(board, myPosition);
            case PAWN -> ChessMoveCalculator.getPawn(board, myPosition);
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessPiece that = (ChessPiece) o;
        return color == that.color && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, type);
    }

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

    private Collection<ChessMove> getQueen(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> queenMoves = getDiagonals(board, myPosition);
        queenMoves.addAll(getCross(board, myPosition));
        return queenMoves;
    }

    private Collection<ChessMove> getPawn(ChessBoard board, ChessPosition start) { //Fail: 10, 11
        Collection<ChessMove> endMoves = new ArrayList<>();
        int pieceDirection = (color == ChessGame.TeamColor.BLACK ? -1 : 1);
        ChessPiece atTemp;
        //Move Forward
        ChessPosition temp = new ChessPosition(start.getRow() + pieceDirection, start.getColumn());

        atTemp = board.getPiece(temp);
        if (atTemp == null) {
            addPawnPromotionMoves(endMoves, start, temp);

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
            if (atTemp != null && atTemp.color != color) {
                addPawnPromotionMoves(endMoves, start, temp);
            }
        }
        return endMoves;
    }

    private void addPawnPromotionMoves(Collection<ChessMove> moves, ChessPosition start, ChessPosition end) {
        if (end.getRow() == (color == ChessGame.TeamColor.BLACK ? 1 : 8)) {
            Collections.addAll(moves, new ChessMove(start, end, PieceType.QUEEN), new ChessMove(start, end, PieceType.ROOK), new ChessMove(start, end, PieceType.BISHOP), new ChessMove(start, end, PieceType.KNIGHT));
        } else {
            moves.add(new ChessMove(start, end, null));
        }
    }

    private Collection<ChessMove> getCross(ChessBoard board, ChessPosition start) {
        Collection<ChessMove> endMoves = new ArrayList<>();
        endMoves.addAll(addMoveList(board, start, 8 - start.getColumn(), 0, +1));
        endMoves.addAll(addMoveList(board, start, start.getColumn() - 1, 0, -1));
        endMoves.addAll(addMoveList(board, start, 8 - start.getRow(), +1, 0));
        endMoves.addAll(addMoveList(board, start, start.getRow() - 1, -1, 0));
        return endMoves;
    }

    private Collection<ChessMove> getDiagonals(ChessBoard board, ChessPosition start) {
        int[] limits = {
            8 - Math.max(start.getRow(), start.getColumn()),
            8 - Math.max(start.getRow(), 9 - start.getColumn()),
            8 - Math.max(9 - start.getRow(), start.getColumn()),
            8 - Math.max(9 - start.getRow(), 9 - start.getColumn())
        };
        Collection<ChessMove> endMoves = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            endMoves.addAll(addMoveList(board, start, limits[i], (i < 2 ? +1 : -1), (i % 2 == 0 ? +1 : -1)));
        }
        return endMoves;
    }

    private Collection<ChessMove> getKing(ChessBoard board, ChessPosition start) {
        int[][] offsets = {
            {0, +1}, {0, -1}, {+1, 0}, {-1, 0}, {+1, +1}, {+1, -1}, {-1, -1}, {-1, +1}
        };
        return new ArrayList<>(addMoveTemps(board, start, offsets));
    }

    private Collection<ChessMove> getKnight(ChessBoard board, ChessPosition start) {
        int[][] offsets = {
            {+2, +1}, {+1, +2}, {-2, +1}, {-1, +2}, {-2, -1}, {-1, -2}, {+2, -1}, {+1, -2}
        };
        return new ArrayList<>(addMoveTemps(board, start, offsets));
    }

    private Collection<ChessMove> addMoveList(ChessBoard board, ChessPosition start, int limit, int rowMod, int colMod) {
        Collection<ChessMove> endMoves = new ArrayList<>();
        for (int i = 1; i <= limit; i++) {
            ChessPosition temp = new ChessPosition(start.getRow() + (i * rowMod), start.getColumn() + (i * colMod));
            ChessPiece atTemp = board.getPiece(temp);
            if (atTemp == null || (atTemp.color != color)) {
                endMoves.add(new ChessMove(start, temp, null));
            }
            if (atTemp != null) { break; }
        }
        return endMoves;
    }

    private Collection<ChessMove> addMoveTemps(ChessBoard board, ChessPosition start, int[][] offsets) {
        Collection<ChessMove> endMoves = new ArrayList<>();
        for (int[] offset : offsets) {
            ChessPosition temp = new ChessPosition(start.getRow() + offset[0], start.getColumn() + offset[1]);
            if (temp.outOfBounds()) continue;
            ChessPiece atTemp = board.getPiece(temp);
            if (atTemp == null || (atTemp.color != color)) {
                endMoves.add(new ChessMove(start, temp, null));
            }
        }
        return endMoves;
    }
}
