package chess.calculator;

public class KingMoveCalculator extends OffsetMoveCalculator {
    @Override
    protected int getDirMod(boolean isRow, boolean flipA, boolean flipB, boolean flipC) {
        boolean b = flipB ^ !isRow;
        return getMod(flipC && b, flipB && b) * getMod(flipA);
    }
}
