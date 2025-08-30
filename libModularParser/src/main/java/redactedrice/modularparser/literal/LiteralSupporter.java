package redactedrice.modularparser.literal;


import redactedrice.modularparser.core.Response;
import redactedrice.modularparser.core.Supporter;

public interface LiteralSupporter extends Supporter {	
    public Response<Object> evaluateLiteral(String literal);

    public Object evaluateChainedLiteral(Object chained, String literal);
}
