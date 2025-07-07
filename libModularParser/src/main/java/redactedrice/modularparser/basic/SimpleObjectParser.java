package redactedrice.modularparser.basic;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import redactedrice.modularparser.LiteralHandler;

public class SimpleObjectParser extends BaseModule implements LiteralHandler {
	private final static Pattern OBJ_ARG_PATTERN = Pattern.compile("(\\w+)\\(([^)]*)\\)");
	private final static String ARG_DELIMITER = ",";

    public SimpleObjectParser() {
        super("SimpleObjectParser");
    }

    @Override
    public Optional<Object> tryEvaluateLiteral(String literal) {
        if (literal == null) {
            return Optional.empty();
        }
        String trimmed = literal.trim();
        
        Matcher m = OBJ_ARG_PATTERN.matcher(trimmed);
        if (!m.find()) {
            return Optional.empty();
        }
        
        String objName = m.group(1);
        String[] args = m.group(2).split(ARG_DELIMITER);
        
        if (!objName.equals("simpleobject")) {
            return Optional.empty();
        }
        
        // parse args and return obj
        
        return Optional.empty();
    }
}
