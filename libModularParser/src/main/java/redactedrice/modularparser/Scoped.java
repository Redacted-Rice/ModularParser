package redactedrice.modularparser;


public interface Scoped {
    String currentScope();

    void pushScope(String scope);

    void popScope();

    void removeScope(String scope);
}
