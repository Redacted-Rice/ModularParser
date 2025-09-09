package redactedrice.modularparser.lineformer;


import redactedrice.modularparser.core.Module;

public interface LineModifier extends Module {
    public boolean lineContinuersValid(String line, boolean isLineComplete);

    public boolean lineHasOpenModifier(String line);

    public String modifyLine(String line);

}
