package redactedrice.modularparser.lineformer;


public interface LineModifier {
    public boolean hasOpenModifier(String line);

    public String modifyLine(String line);
}
