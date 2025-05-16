package chess.calculator;

public class KnightMoveCalculator extends OffsetMoveCalculator {
    @Override
    protected int getDirMod(boolean isRow, boolean flipA, boolean flipB, boolean flipC) {
        return (flipC ^ !isRow ? 1 : 2) * getMod(isRow ? flipA : flipB);
    }
}
