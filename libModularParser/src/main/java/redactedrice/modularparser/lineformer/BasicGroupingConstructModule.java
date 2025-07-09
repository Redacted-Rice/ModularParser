package redactedrice.modularparser.lineformer;


import redactedrice.modularparser.BaseModule;

public class BasicGroupingConstructModule extends BaseModule implements LineModifier {
    protected static final String NEWLINE_REGEX = "\\s*\\R\\s*";

    protected final String startToken;
    protected final String endToken;
    protected final boolean removeTokens;
    protected final String startTokenRegex;
    protected final String endTokenRegex;

    public BasicGroupingConstructModule(String name, String startToken, String endToken,
            boolean removeTokens) {
        super(name);
        this.startToken = startToken;
        this.endToken = endToken;
        this.removeTokens = removeTokens;
        startTokenRegex = "\\s*" + startToken + "\\s*";
        endTokenRegex = "\\s*" + endToken + "\\s*";
    }

    @Override
    public boolean hasOpenModifier(String line) {
        return countOccurrences(line, startToken) != countOccurrences(line, endToken);
    }

    @Override
    public String modifyLine(String line) {
        // We only need to test for one because it will have both
        if (line.contains(startTokenRegex)) {
            line = line.replaceAll(NEWLINE_REGEX, " ");
            if (removeTokens) {
                line = line.replaceAll(startTokenRegex, " ").replaceAll(endTokenRegex, " ");
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
