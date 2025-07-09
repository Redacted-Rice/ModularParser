package redactedrice.modularparser.literal;


public interface LiteralSupporter {
	public void addLiteralParser(LiteralHandler parser);

    public Object evaluateLiteral(String literal);

    public Object evaluateChainedLiteral(Object chained, String literal);
}
