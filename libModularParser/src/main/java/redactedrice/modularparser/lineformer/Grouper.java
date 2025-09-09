package redactedrice.modularparser.lineformer;


import redactedrice.modularparser.core.Module;
import redactedrice.modularparser.core.Response;

public interface Grouper extends Module {
    public Response<String> getIfCompleteGroup(String line);
}
