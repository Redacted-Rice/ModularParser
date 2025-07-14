package redactedrice.modularparser.lineformer;


import redactedrice.modularparser.core.Module;

public interface LineModifier extends Module {
    public boolean hasOpenModifier(String line);

    public String modifyLine(String line);
}
