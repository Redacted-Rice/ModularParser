package redactedrice.modularparser.lineformer;


import redactedrice.modularparser.core.BaseModule;

public class LineContinuerLineModifierModule extends BaseModule implements LineModifier {
    protected final String token;
    protected final String tokenRegex;
    protected final String replaceStr;

    public LineContinuerLineModifierModule(String name, String token, boolean removeToken) {
        super(name);
        this.token = token;
        tokenRegex = "\\s*" + token + "\\s*";
        if (removeToken) {
            replaceStr = " ";
        } else {
            replaceStr = " " + token + " ";
        }
    }

    @Override
    public boolean hasOpenModifier(String line) {
        return line.endsWith(token);
    }

    @Override
    public String modifyLine(String line) {
        return line.replaceAll(tokenRegex, replaceStr);
    }
}
