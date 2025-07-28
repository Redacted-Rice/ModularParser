package redactedrice.modularparser.comment;


import redactedrice.modularparser.core.BaseModule;
import redactedrice.modularparser.lineformer.LineModifier;

public class DefaultSingleLineCommentLineModifier extends BaseModule implements LineModifier {
    protected final String token;

    public DefaultSingleLineCommentLineModifier(String name, String token) {
        super(name);
        this.token = token;
    }

    @Override
    public boolean hasOpenModifier(String line) {
        return false;
    }

    @Override
    public String modifyLine(String line) {
        int idx = line.indexOf(token);
        if (idx >= 0) {
            line = line.substring(0, idx);
        }
        return line;
    }
}
