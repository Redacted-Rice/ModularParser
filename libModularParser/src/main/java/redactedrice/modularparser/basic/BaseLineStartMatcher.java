package redactedrice.modularparser.basic;


import redactedrice.modularparser.lineparser.LineParser;
import redactedrice.modularparser.reserved.BaseWordReserver;

public abstract class BaseLineStartMatcher extends BaseWordReserver implements LineParser {
    protected final String keyword;

    protected BaseLineStartMatcher(String name, String keyword) {
        super(name);

        this.keyword = keyword;
        reservedWords.put(keyword, ReservedType.EXCLUSIVE);
    }
    
    public boolean matches(String logicalLine) {
        if (logicalLine == null || logicalLine.isBlank()) {
            return false;
        }

        String[] words = logicalLine.trim().split("\\s+", 2);
        return words[0].equals(keyword);
    }
}