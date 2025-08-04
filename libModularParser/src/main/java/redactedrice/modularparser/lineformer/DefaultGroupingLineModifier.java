package redactedrice.modularparser.lineformer;


import java.util.regex.Pattern;

import redactedrice.modularparser.core.BaseModule;
import redactedrice.modularparser.core.LogSupporter.LogLevel;

public class DefaultGroupingLineModifier extends BaseModule implements LineModifier {
    protected static final Pattern NEWLINE_PATTERN = Pattern.compile("\\s*\\R\\s*");

    protected final String startToken;
    protected final String endToken;
    protected final boolean removeTokens;
    protected final Pattern startTokenRegex;
    protected final Pattern endTokenRegex;

    public DefaultGroupingLineModifier(String name, String startToken, String endToken,
            boolean removeTokens) {
        super(name);
        this.startToken = startToken;
        this.endToken = endToken;
        this.removeTokens = removeTokens;
        startTokenRegex = Pattern.compile("\\s*" + Pattern.quote(startToken) + "\\s*");
        endTokenRegex = Pattern.compile("\\s*" + Pattern.quote(endToken) + "\\s*");
    }

    @Override
    public boolean lineContinuersValid(String line, boolean isComplete) {
        String error = LineModifier.validStartStopTokens(line, startToken, endToken, isComplete);
        if (!error.isEmpty()) {
            log(LogLevel.ERROR, error);
            return false;
        }
        return true;
    }

    @Override
    public boolean lineHasOpenModifier(String line) {
        return countOccurrences(line, startToken) > countOccurrences(line, endToken);
    }

    @Override
    public String modifyLine(String line) {
        // We only need to test for one because it will have both
        if (line.contains(startToken)) {
            line = NEWLINE_PATTERN.matcher(line).replaceAll(" ");
            if (removeTokens) {
                line = endTokenRegex.matcher(startTokenRegex.matcher(line).replaceAll(" "))
                        .replaceAll(" ");
            } else {
                line = endTokenRegex
                        .matcher(startTokenRegex.matcher(line).replaceAll(" " + startToken))
                        .replaceAll(endToken + " ");
            }
        }
        return line;
    }

    public static int countOccurrences(String str, String sub) {
        int count = 0;
        int index = 0;

        while ((index = str.indexOf(sub, index)) != -1) {
            count++;
            index += sub.length(); // move past the current match
        }

        return count;
    }
}
