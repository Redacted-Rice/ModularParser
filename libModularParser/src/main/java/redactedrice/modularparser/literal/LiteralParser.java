package redactedrice.modularparser.literal;


import redactedrice.modularparser.core.Module;
import redactedrice.modularparser.core.Response;

public interface LiteralParser extends Module {
    public Response<Object> tryParseLiteral(String literal);
}
