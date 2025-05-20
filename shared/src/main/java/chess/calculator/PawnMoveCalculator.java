package chess.calculator;

import chess.*;

import java.util.Collection;

import static chess.ChessGame.*;

public class PawnMoveCalculator extends ProgrammaticMoveCalculator {
    @Override
    protected boolean collectMovesInDirection(
            ChessBoard board, ChessPosition start, Collection<ChessMove> endMoves,
            boolean flipA, boolean flipB, boolean flipC) {
        ChessGame.TeamColor color = board.getPiece(start).color();
        flipC = color == TeamColor.BLACK;
        for (int i = 1; i <= getLimit(start, flipA, flipB); i++) {
            ChessPosition temp = new ChessPosition(
                    start.row() + getDirMod(true, flipA, flipB, flipC),
                    start.col() + getDirMod(false, flipA, flipB, flipC));
            ChessPiece atTemp = board.getPiece(temp);
            if (flipA && atTemp != null) {
                return true;
            }
            if (flipA || atTemp != null && atTemp.color() != color) {
                ChessPiece.PieceType[] pieces = temp.getRow() == getTeamInitialRow(getOtherTeam(color))
                        ? promotions : new ChessPiece.PieceType[] { null };
                for (var pieceType : pieces) {
                    endMoves.add(new ChessMove(start, temp, pieceType));
                }
            }
            if (flipA && start.getRow() != getTeamInitialRow(color) + getTeamDirection(color)) {
                return true;
            }
        }
        return false;
    }

    protected int getLimit(ChessPosition start, boolean flipA, boolean flipB) {
        return 1;
    }

    @Override
    protected boolean ignoreThird() {
        return true;
    }

    protected int getDirMod(boolean isRow, boolean flipA, boolean flipB, boolean flipC) {
        return isRow ? getOffset(flipA && flipB) * getMod(flipC) : getMod(flipA, flipB);
    }

    private static final ChessPiece.PieceType[] promotions = {
            ChessPiece.PieceType.QUEEN, ChessPiece.PieceType.KNIGHT,
            ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.ROOK
    };
}
