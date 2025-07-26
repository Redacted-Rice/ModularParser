package redactedrice.modularparser.scope;


import redactedrice.modularparser.core.BaseModule;
import redactedrice.modularparser.core.ModularParser;

public abstract class BaseScopedParser extends BaseModule implements ScopedParser {
    protected ScopeSupporter scopeSupporter;

    protected BaseScopedParser(ModularParser parser, String name) {
        super(parser, name);
    }

    @Override
    public void setModuleRefs() {
        super.setModuleRefs();
        scopeSupporter = parser.getSupporterOfType(ScopeSupporter.class);
    }
}
