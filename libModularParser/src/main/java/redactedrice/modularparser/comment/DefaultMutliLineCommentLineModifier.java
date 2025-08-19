package redactedrice.modularparser.comment;


import redactedrice.modularparser.core.BaseModule;
import redactedrice.modularparser.core.LogSupporter.LogLevel;
import redactedrice.modularparser.lineformer.LineModifier;

public class DefaultMutliLineCommentLineModifier extends BaseModule implements LineModifier {
    protected final String startToken;
    protected final String endToken;

    public DefaultMutliLineCommentLineModifier(String name, String startToken, String endToken) {
        super(name);
        this.startToken = startToken;
        this.endToken = endToken;
    }

    protected String recursivelyRemoveTokenPairs(String line, boolean openOk) {
        int endIdx = line.indexOf(endToken);
        int startIdx = line.indexOf(startToken);
        // if both are < 0, then there are none or if only the end is not
        // found and we allow open, then we are good
        if ((endIdx < 0 && startIdx < 0) || (endIdx < 0 && openOk)) {
            return line;
        } else if (endIdx >= 0 && startIdx >= 0) {
            if (endIdx < startIdx) {
                return null;
            }
            // Strip the token pair and keep going
            return recursivelyRemoveTokenPairs(removeTokenPair(line, startIdx, endIdx), openOk);
        }
        return null;
    }

    protected String removeTokenPair(String line, int startIdx, int endIdx) {
        // Build a string with the comment replaced by a space
        String lineStart = line.substring(0, startIdx);
        boolean endsWithWhitespace = !lineStart.isEmpty() &&
                Character.isWhitespace(lineStart.charAt(lineStart.length() - 1));
        String lineEnd = line.substring(endIdx + endToken.length());
        boolean startsWithWhitespace = !lineEnd.isEmpty() &&
                Character.isWhitespace(lineEnd.charAt(0));

        StringBuilder sb = new StringBuilder(lineStart.trim());
        if (endsWithWhitespace || startsWithWhitespace) {
            sb.append(' ');
        }
        sb.append(lineEnd.trim());
        return sb.toString();
    }

    @Override
    public boolean lineContinuersValid(String line, boolean isComplete) {
        return null != recursivelyRemoveTokenPairs(line, !isComplete);
    }

    @Override
    public boolean lineHasOpenModifier(String line) {
        int endIdx = line.lastIndexOf(endToken);
        int startIdx = line.lastIndexOf(startToken);
        return startIdx >= 0 && startIdx > endIdx;
    }

    @Override
    public String modifyLine(String line) {
        String stripped = recursivelyRemoveTokenPairs(line, false);
        if (stripped == null) {
            log(LogLevel.ERROR, "Lineformer logic errror detected: Passed an invalid line!\n%s",
                    line);
            return line;
        }
        return stripped;
    }

}
