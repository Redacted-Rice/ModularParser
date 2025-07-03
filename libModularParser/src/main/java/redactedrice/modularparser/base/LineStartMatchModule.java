package redactedrice.modularparser.base;


import redactedrice.modularparser.LineHandler;

public abstract class LineStartMatchModule extends ReservedWordModule implements LineHandler {
    protected final String keyword;

    protected LineStartMatchModule(String name, String keyword) {
        super(name);

        this.keyword = keyword;
        reservedWords.put(keyword, ReservedType.EXCLUSIVE);
    }

    @Override
    public boolean matches(String logicalLine) {
        if (logicalLine == null || logicalLine.isBlank()) {
            return false;
        }

        String[] words = logicalLine.trim().split("\\s+", 2);
        return words[0].equals(keyword);
    }
}