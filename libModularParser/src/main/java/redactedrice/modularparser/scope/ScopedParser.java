package redactedrice.modularparser.scope;


import redactedrice.modularparser.core.Module;

public interface ScopedParser extends Module {
    public abstract boolean tryParseScoped(String scope, String logicalLine, String defaultScope);
}
