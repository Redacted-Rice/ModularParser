package redactedrice.modularparser.literal;


import redactedrice.modularparser.core.BaseModule;
import redactedrice.modularparser.core.Response;

public class DefaultBoolLiteralParser extends BaseModule implements LiteralParser {
    public DefaultBoolLiteralParser() {
        super(DefaultBoolLiteralParser.class.getSimpleName());
    }

    @Override
    public Response<Object> tryParseLiteral(String literal) {
        String trimmed = literal.trim().toLowerCase();
        return switch (trimmed) {
        case "true", "t" -> Response.is(true);
        case "false", "f" -> Response.is(false);
        default -> Response.notHandled(); // let another parse try to handle it
        };
    }
}
