package redactedrice.modularparser.literal.named;


import java.util.Map;

import redactedrice.modularparser.literal.LiteralParser;

public interface NamedLiteralParser extends LiteralParser {
    boolean isVariable(String var);

    Map<String, Object> getVariables();
}
