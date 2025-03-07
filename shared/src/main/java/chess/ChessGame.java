package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard gameBoard = new ChessBoard();
    private TeamColor currentTurn;
    private boolean inPlay;

    public ChessGame() {
        gameBoard.resetBoard();
        currentTurn = TeamColor.WHITE;
        inPlay = true;
    }

    public boolean gameInPlay() { return inPlay; }
    public void endGame() { inPlay = false; }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        currentTurn = team;
    }
    private void changeTurn() {
        setTeamTurn(getOtherTeam(currentTurn));
    }

    private TeamColor getOtherTeam(TeamColor teamColor) {
        if (teamColor == TeamColor.WHITE) {
            return TeamColor.BLACK;
        } else {
            return TeamColor.WHITE;
        }
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece temp = gameBoard.getPiece(startPosition);
        if (temp == null) {
            return null;
        }
        return removeInvalidMoves(temp.pieceMoves(gameBoard, startPosition));
    }

    private Collection<ChessMove> removeInvalidMoves(Collection<ChessMove> moves) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        for (ChessMove test : moves) {
            if (!moveLeavesInCheck(test)) {
                validMoves.add(test);
            }
        }
        return validMoves;
    }

    private boolean moveLeavesInCheck(ChessMove move) {
        ChessBoard testBoard = gameBoard.clone();
        TeamColor colorToCheck = gameBoard.getPiece(move.getStartPosition()).getTeamColor();
        makeMoveInGame(move, testBoard);
        return isInCheckTest(colorToCheck, testBoard);
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        if (!inPlay) {
            throw new InvalidMoveException("The game has already ended.");
        }
        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());
        if (validMoves == null || !validMoves.contains(move)) {
            throw new InvalidMoveException("Move chosen is illegal.");
        }
        if (gameBoard.getPiece(move.getStartPosition()).getTeamColor() != currentTurn) {
            throw new InvalidMoveException("Piece not part of current turn's color.");
        }
        makeMoveInGame(move, gameBoard);
        changeTurn();
    }

    private void makeMoveInGame(ChessMove move, ChessBoard board) {
        if (move.getPromotionPiece() == null) {
            board.addPiece(move.getEndPosition(), board.getPiece(move.getStartPosition()));
        } else {
            board.addPiece(move.getEndPosition(), new ChessPiece(board.getPiece(move.getStartPosition()).getTeamColor(), move.getPromotionPiece()));
        }
        board.addPiece(move.getStartPosition(), null);
    }


    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return isInCheckTest(teamColor, gameBoard);
    }

    private boolean isInCheckTest(TeamColor teamColor, ChessBoard board) {
        Collection<ChessMove> allOpposingMoves = allPossibleTeamMoves(getOtherTeam(teamColor), board);
        for (ChessMove move : allOpposingMoves) {
            ChessPiece temp = board.getPiece(move.getEndPosition());
            if (temp != null && temp.getTeamColor() == teamColor && temp.getPieceType() == ChessPiece.PieceType.KING) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor) && allPossibleValidMoves(teamColor).isEmpty();
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor) && allPossibleValidMoves(teamColor).isEmpty();
    }

    private Collection<ChessMove> allPossibleTeamMoves(TeamColor team, ChessBoard board) {
        Collection<ChessMove> allMoves = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition temp = new ChessPosition(i,j);
                ChessPiece atTemp = board.getPiece(temp);
                if (atTemp != null && atTemp.getTeamColor() == team) {
                    allMoves.addAll(atTemp.pieceMoves(board, temp));
                }
            }
        }
        return allMoves;
    }

    private Collection<ChessMove> allPossibleValidMoves(TeamColor team) {
        return removeInvalidMoves(allPossibleTeamMoves(team, gameBoard));
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        gameBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return gameBoard;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(gameBoard, chessGame.gameBoard) && currentTurn == chessGame.currentTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameBoard, currentTurn);
    }
}
