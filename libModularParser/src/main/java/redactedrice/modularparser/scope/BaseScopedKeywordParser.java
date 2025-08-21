package redactedrice.modularparser.scope;


import redactedrice.modularparser.reserved.BaseKeywordReserver;

public abstract class BaseScopedKeywordParser extends BaseKeywordReserver implements ScopedParser {
    protected ScopeSupporter scopeSupporter;

    protected BaseScopedKeywordParser(String name, String keyword) {
        super(name, keyword);
    }

    @Override
    public void setModuleRefs() {
        super.setModuleRefs();
        scopeSupporter = parser.getSupporterOfType(ScopeSupporter.class);
    }
}
