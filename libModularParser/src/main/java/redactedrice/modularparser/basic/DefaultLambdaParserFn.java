package redactedrice.modularparser.basic;


// To support simple Lambda Handlers mostly for testing
@FunctionalInterface
public interface DefaultLambdaParserFn {
    // Handles the passed logical line
    void handle(String logicalLine);
}