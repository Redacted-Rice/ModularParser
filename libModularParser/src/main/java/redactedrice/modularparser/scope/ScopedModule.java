package redactedrice.modularparser.scope;


import redactedrice.modularparser.LineHandler;
import redactedrice.modularparser.basic.ReservedWordModule;

public abstract class ScopedModule extends ReservedWordModule implements LineHandler {
	protected final ScopeHandler scopeHandler;
	
	protected ScopedModule(String name, ScopeHandler scopeHandler) {
		super(name);
		this.scopeHandler = scopeHandler;
		scopeHandler.addScopedModule(getName());
	}
	
    @Override
    public boolean matches(String logicalLine) {
        String[] split = scopeHandler.splitScope(logicalLine);
        if (split == null) {
            return false;
        }
        
        return scopedMatches(split[0], split[1]);
    }
    
    public abstract boolean scopedMatches(String scope, String logicalLine);

	@Override
	public void handle(String logicalLine) {
        String[] scopeLine = scopeHandler.splitScope(logicalLine);
        if (scopeLine.length <= 0) {
            return;
        }
        
		scopedHandle(scopeLine[0], scopeLine[1], scopeHandler.currentScope());
	}
	
    public abstract void scopedHandle(String scope, String logicalLine, String defaultScope);
}
