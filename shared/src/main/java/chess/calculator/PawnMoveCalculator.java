package chess.calculator;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;

import static chess.ChessGame.*;

public class PawnMoveCalculator extends ProgrammaticMoveCalculator {
    @Override
    protected boolean collectMovesInDirection(ChessBoard board, ChessPosition start, Collection<ChessMove> endMoves, boolean forward, boolean dir, boolean ignored) {
        ChessGame.TeamColor color = board.getPiece(start).color();
        int pieceDirection = getTeamDirection(color);
        ChessPosition temp = new ChessPosition(start.getRow() + getOffset(forward && dir) * pieceDirection, start.getColumn() + getMod(forward, dir));
        ChessPiece atTemp = board.getPiece(temp);
        if (forward && atTemp != null) { return true; }
        if (forward || atTemp != null && atTemp.color() != color) {
            ChessPiece.PieceType[] pieces = temp.getRow() == getTeamInitialRow(getOtherTeam(color))
                    ? promotions : new ChessPiece.PieceType[] {null};
            for (var pieceType : pieces) {
                endMoves.add(new ChessMove(start, temp, pieceType));
            }
        }
        return forward && start.getRow() != getTeamInitialRow(color) + pieceDirection;
    }

    @Override
    protected boolean ignoreThird() {
        return true;
    }

    private static final ChessPiece.PieceType[] promotions = {
            ChessPiece.PieceType.QUEEN, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.ROOK
    };
}
