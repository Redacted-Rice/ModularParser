package redactedrice.modularparser.lineformer;


public interface LineFormerSupporter {
    public void addLineModifier(LineModifier modifier);

    public String getNextLogicalLine();
}
