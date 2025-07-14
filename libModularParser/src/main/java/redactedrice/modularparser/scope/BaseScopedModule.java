package redactedrice.modularparser.scope;

import redactedrice.modularparser.core.BaseModule;

public abstract class BaseScopedModule extends BaseModule implements ScopedParser {
	protected ScopeSupporter scopeSupporter;
	
    protected BaseScopedModule(String name) {
		super(name);
	}
    
    @Override
    public void configure() {
    	scopeSupporter = parser.getSupporterOfType(ScopeSupporter.class);
    }
}
