package redactedrice.modularparser.literals;


public interface LiteralSupporter {
    void addLiteralParser(LiteralParser parser);

    public Object evaluateLiteral(String literal);
}
