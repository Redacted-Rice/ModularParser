package redactedrice.modularparser.base;


// To support simple Lambda Handlers mostly for testing
@FunctionalInterface
public interface ModuleLambdaFn {
    // Handles the passed logical line
    void handle(String logicalLine);
}