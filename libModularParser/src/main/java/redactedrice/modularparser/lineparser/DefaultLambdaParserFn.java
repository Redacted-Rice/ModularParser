package redactedrice.modularparser.lineparser;


// To support simple Lambda Handlers mostly for testing
@FunctionalInterface
public interface DefaultLambdaParserFn {
    // Handles the passed logical line
    void handle(String logicalLine);
}