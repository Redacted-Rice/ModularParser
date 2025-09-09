package redactedrice.modularparser.lineformer;


import redactedrice.modularparser.core.Module;
import redactedrice.modularparser.core.Response;

public interface Grouper extends Module {
    public boolean isEmptyGroup(String string);

    public boolean hasOpenGroup(String line);

    public Response<String[]> tryGetNextGroup(String line, boolean stripTokens);
}
