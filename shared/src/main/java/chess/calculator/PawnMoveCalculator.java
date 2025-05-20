package chess.calculator;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;

import static chess.ChessGame.*;

public class PawnMoveCalculator extends ProgrammaticMoveCalculator {
    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition start) {
        Collection<ChessMove> endMoves = new ArrayList<>();
        ChessGame.TeamColor color = board.getPiece(start).color();
        int pieceDirection = getTeamDirection(color);

        for (boolean forward : options) {
            for (boolean dir : options) {
                ChessPosition temp = new ChessPosition(start.getRow() + getOffset(forward && dir) * pieceDirection, start.getColumn() + getMod(forward, dir));
                ChessPiece atTemp = board.getPiece(temp);
                if (forward && atTemp != null) { break; }
                if (forward || atTemp != null && atTemp.color() != color) {
                    ChessPiece.PieceType[] pieces = temp.getRow() == getTeamInitialRow(getOtherTeam(color))
                            ? promotions : new ChessPiece.PieceType[] {null};
                    for (var pieceType : pieces) {
                        endMoves.add(new ChessMove(start, temp, pieceType));
                    }
                }
                if (forward && start.getRow() != getTeamInitialRow(color) + pieceDirection) { break; }
            }
        }

        return endMoves;
    }

    private static final ChessPiece.PieceType[] promotions = {
            ChessPiece.PieceType.QUEEN, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.ROOK
    };
}
