package redactedrice.modularparser.basic;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import redactedrice.modularparser.LiteralHandler;

public class SimpleObjectParser extends BaseModule implements LiteralHandler {
    private final static Pattern OBJ_ARG_PATTERN = Pattern.compile("(\\w+)\\(([^)]*)\\)");
    private final static String ARG_DELIMITER = ",";
    private final static String ARG_NAME_DELIMITER = " ";

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

        if (!objName.toLowerCase().equals("simpleobject")) {
            return Optional.empty();
        }

        // parse args and return obj
        List<String> positionalParams = new LinkedList<>();
        Map<String, String> namedParams = new HashMap<>();
        for (String arg : args) {
            arg = arg.trim();
            String[] argSplit = arg.split(ARG_NAME_DELIMITER, 2);
            if (argSplit.length == 1 || arg.startsWith("\"") && arg.endsWith("\"")) {
                positionalParams.add(arg);
            } else {
                namedParams.put(argSplit[0], argSplit[1]);
            }
        }

        String[] argNames = new String[] { "intVal", "boolVal", "strVal"
        };
        Object[] parsedArgs = new Object[] { 0, false, ""
        };

        for (int count = 0; count < positionalParams.size(); count++) {
            parsedArgs[count] = parser.evaluateLiteral(positionalParams.get(count));
        }

        for (int count = 0; count < argNames.length; count++) {
            if (namedParams.containsKey(argNames[count])) {
                parsedArgs[count] = parser.evaluateLiteral(namedParams.get(argNames[count]));
            }
        }

        return Optional.of(new SimpleObject((int) parsedArgs[0], (boolean) parsedArgs[1],
                (String) parsedArgs[2]));
    }
}
