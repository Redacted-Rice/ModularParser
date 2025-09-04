package redactedrice.modularparser.literal;


import redactedrice.modularparser.core.BaseModule;
import redactedrice.modularparser.core.Response;

public class DefaultCharLiteralParser extends BaseModule implements LiteralParser {
    public DefaultCharLiteralParser() {
        super(DefaultCharLiteralParser.class.getSimpleName());
    }

    @Override
    public Response<Object> tryParseLiteral(String literal) {
        String trimmed = literal.trim();

        // Double-quoted string
        if (trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            String body = trimmed.substring(1, trimmed.length() - 1).replaceAll("\\\"", "\"");
            return Response.is(body);
        }

        // Single-quoted char
        if (trimmed.length() >= 3 && trimmed.startsWith("'") && trimmed.endsWith("'")) {
            String body = trimmed.substring(1, trimmed.length() - 1);
            if (body.length() == 1) {
                return Response.is(body.charAt(0));
            }
        }
        // Let another parser try to handle it
        return Response.notHandled();
    }
}
