package redactedrice.modularparser.comment;


import redactedrice.modularparser.lineformer.LineModifier;
import redactedrice.modularparser.log.BaseLoggingModule;
import redactedrice.modularparser.log.LogSupporter.LogLevel;

public class DefaultMutliLineCommentLineModifier extends BaseLoggingModule implements LineModifier {
    protected final String startToken;
    protected final String endToken;

    public DefaultMutliLineCommentLineModifier(String name, String startToken, String endToken) {
        super(name);
        this.startToken = startToken;
        this.endToken = endToken;
    }

    @Override
    public boolean hasOpenModifier(String line) {
        return line.contains(startToken) && !line.contains(endToken);
    }

    @Override
    public String modifyLine(String line) {
        // Do we have a start token?
        int startIdx = line.indexOf(startToken);
        // No comment, nothing to do
        if (startIdx < 0) {
            return line;
        }

        // We have a comment and should have a end too
        int endIdx = line.indexOf(endToken);
        if (endIdx < 0) {
            logger.log(LogLevel.ERROR,
                    "ModularParser logic errror detected: Passed a line with a start token and not an end token!\n%s",
                    line);
            return line;
        }

        // Build a string with the comment replaced by a space
        StringBuilder sb = new StringBuilder(line.substring(0, startIdx).trim());
        sb.append(' ');
        sb.append(line.substring(endIdx + endToken.length()).trim());
        return sb.toString();
    }
}
