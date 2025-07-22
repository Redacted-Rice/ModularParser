package redactedrice.modularparser.scope;


import redactedrice.modularparser.log.BaseLoggingModule;

public abstract class BaseScopedParser extends BaseLoggingModule implements ScopedParser {
    protected ScopeSupporter scopeSupporter;

    protected BaseScopedParser(String name) {
        super(name);
    }

    @Override
    public void setModuleRefs() {
        super.setModuleRefs();
        scopeSupporter = parser.getSupporterOfType(ScopeSupporter.class);
    }
}
