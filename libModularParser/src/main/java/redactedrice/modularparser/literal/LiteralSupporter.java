package redactedrice.modularparser.literal;


import redactedrice.modularparser.core.Supporter;

public interface LiteralSupporter extends Supporter {
    public Object evaluateLiteral(String literal);

    public Object evaluateChainedLiteral(Object chained, String literal);
}
