package redactedrice.modularparser.scope;


import redactedrice.modularparser.core.LogSupporter.LogLevel;
import redactedrice.modularparser.reserved.BaseKeywordReserver;
import redactedrice.modularparser.reserved.ReservedWordSupporter;

public abstract class BaseScopedKeywordParser extends BaseKeywordReserver implements ScopedParser {
    protected ScopeSupporter scopeSupporter;
    protected ReservedWordSupporter reservedWordSupporter;

    protected BaseScopedKeywordParser(String name, String keyword) {
        super(name, keyword);
    }

    @Override
    public void setModuleRefs() {
        super.setModuleRefs();
        scopeSupporter = parser.getSupporterOfType(ScopeSupporter.class);
        reservedWordSupporter = parser.getSupporterOfType(ReservedWordSupporter.class);
    }

    public static boolean isValidName(String name) {
        return name != null && name.matches("^[a-zA-Z_]\\w*$");
    }

    public boolean ensureWordAvailableOrOwned(String name) {
        // Check for collisions with reserved words. We get all since we are reserving
        // them exclusively we can't share them
        String reservedOwner = reservedWordSupporter.getReservedWordOwner(name);
        if (reservedOwner != null && !reservedOwner.equals(scopeSupporter.getName())) {
            log(LogLevel.ERROR, "Name '%s' is reserved by '%s' and cannot be shared!", name,
                    reservedOwner);
            return false;
        }
        return true;
    }
}
