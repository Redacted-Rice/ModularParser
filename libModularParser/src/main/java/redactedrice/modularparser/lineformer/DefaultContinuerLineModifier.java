package redactedrice.modularparser.lineformer;


import java.util.regex.Pattern;

import redactedrice.modularparser.core.BaseModule;

public class DefaultContinuerLineModifier extends BaseModule implements LineModifier {
    protected final String token;
    protected final Pattern tokenRegex;
    protected final String replaceStr;

    public DefaultContinuerLineModifier(String name, String token, boolean removeToken) {
        super(name);
        this.token = token;
        tokenRegex = Pattern.compile("\\s*" + Pattern.quote(token) + "\\s*");
        if (removeToken) {
            replaceStr = " ";
        } else {
            replaceStr = " " + token + " ";
        }
    }

    @Override
    public boolean lineContinuersValid(String line, boolean isComplete) {
        return true;
    }

    @Override
    public boolean lineHasOpenModifier(String line) {
        return line.endsWith(token);
    }

    @Override
    public String modifyLine(String line) {
        return tokenRegex.matcher(line).replaceAll(replaceStr);
    }
}
