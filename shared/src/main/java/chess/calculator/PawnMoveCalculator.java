package chess.calculator;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;

import static chess.ChessGame.*;

public class PawnMoveCalculator extends PieceMoveCalculator {
    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition start) {
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
                endMoves.add(new ChessMove(start, temp));
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
}
