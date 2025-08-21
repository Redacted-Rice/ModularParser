package redactedrice.modularparser.reserved;


import java.util.Set;

import redactedrice.modularparser.core.BaseModule;

public class BaseKeywordReserver extends BaseModule implements WordReserver {
    protected final String keyword;

    protected BaseKeywordReserver(String name, String keyword) {
        super(name);
        this.keyword = keyword;
    }

    @Override
    public boolean isReservedWord(String word) {
        return word.equals(getKeyword());
    }

    @Override
    public Set<String> getReservedWords() {
        return Set.of(getKeyword());
    }

    public String getKeyword() {
        return keyword;
    }
}
