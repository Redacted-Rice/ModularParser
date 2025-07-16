package redactedrice.modularparser.scope;


import redactedrice.modularparser.core.BaseModule;

public abstract class BaseScopedParser extends BaseModule implements ScopedParser {
    protected ScopeSupporter scopeSupporter;

    protected BaseScopedParser(String name) {
        super(name);
    }

    @Override
    public void setModuleRefs() {
        scopeSupporter = parser.getSupporterOfType(ScopeSupporter.class);
    }
}
