package chess.calculator;

public class KingMoveCalculator extends OffsetMoveCalculator {
    @Override
    protected int getDirMod(boolean isRow, boolean flipA, boolean flipB, boolean flipC) {
        return getMod(flipC && flipB == isRow, flipB && isRow) * getMod(flipA);
    }
}
