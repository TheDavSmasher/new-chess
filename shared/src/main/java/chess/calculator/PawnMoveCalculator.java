package chess.calculator;

import chess.*;

import java.util.Collection;

import static chess.ChessGame.*;

public class PawnMoveCalculator extends ProgrammaticMoveCalculator {
    @Override
    protected boolean collectMovesInDirection(
            ChessBoard board, ChessPosition start, Collection<ChessMove> endMoves,
            boolean flipA, boolean flipB, boolean ignored) {
        return super.collectMovesInDirection(board, start, endMoves, flipA, flipB,
                board.getPiece(start).color() == TeamColor.BLACK);
    }

    protected Boolean checkAndAdd(Collection<ChessMove> endMoves, ChessBoard board,
                                  ChessPosition start, ChessPosition temp, boolean flipA) {
        ChessPiece atTemp = board.getPiece(temp);
        TeamColor color = board.getPiece(start).color();
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
        return flipA && start.getRow() != getTeamInitialRow(color) + getTeamDirection(color);
    }

    @Override
    protected int getLimit(ChessPosition start, boolean flipA, boolean flipB){
        return 1;
    }

    @Override
    protected boolean ignoreThird(){
        return true;
    }

    @Override
    protected int getDirMod(boolean isRow, boolean flipA, boolean flipB, boolean flipC) {
        return isRow ? getOffset(flipA && flipB) * getMod(flipC) : getMod(flipA, flipB);
    }

    private static final ChessPiece.PieceType[] promotions = {
            ChessPiece.PieceType.QUEEN, ChessPiece.PieceType.KNIGHT,
            ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.ROOK
    };
}
