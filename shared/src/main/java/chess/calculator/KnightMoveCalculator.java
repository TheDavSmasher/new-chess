package chess.calculator;

public class KnightMoveCalculator extends OffsetMoveCalculator {
    @Override
    protected int getDirMod(boolean isRow, boolean flipA, boolean flipB, boolean flipC) {
        return getOffset(flipC != isRow) * getMod(isRow ? flipA : flipB);
    }
}
