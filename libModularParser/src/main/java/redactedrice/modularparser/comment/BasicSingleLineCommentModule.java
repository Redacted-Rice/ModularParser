package redactedrice.modularparser.comment;


import redactedrice.modularparser.BaseModule;
import redactedrice.modularparser.lineformer.LineModifier;

public class BasicSingleLineCommentModule extends BaseModule implements LineModifier {
    protected final String token;

    public BasicSingleLineCommentModule(String name, String token) {
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
