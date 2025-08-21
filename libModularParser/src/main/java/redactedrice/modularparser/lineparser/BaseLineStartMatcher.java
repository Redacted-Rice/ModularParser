package redactedrice.modularparser.lineparser;


import redactedrice.modularparser.reserved.BaseKeywordReserver;

public abstract class BaseLineStartMatcher extends BaseKeywordReserver implements LineParser {

    protected BaseLineStartMatcher(String name, String keyword) {
        super(name, keyword);
    }

    public boolean matches(String logicalLine) {
        if (logicalLine == null || logicalLine.isBlank()) {
            return false;
        }

        String[] words = logicalLine.trim().split("\\s+", 2);
        return words[0].equals(getKeyword());
    }
}