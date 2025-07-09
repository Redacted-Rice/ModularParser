package redactedrice.modularparser.literal.named;


import java.util.Map;

import redactedrice.modularparser.literal.LiteralHandler;

public interface NamedLiteralHandler extends LiteralHandler {
    boolean isVariable(String var);

    Map<String, Object> getVariables();
}
