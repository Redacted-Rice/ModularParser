package redactedrice.modularparser.literal;


import redactedrice.modularparser.core.Response;

public interface ChainableLiteralParser extends LiteralParser {
    Response<Object> tryEvaluateChainedLiteral(Object chained, String literal);
}
