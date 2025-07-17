package redactedrice.modularparser.core;


public interface LineFormerSupporter extends Supporter {
    public record LogicalLine(String line, int number) {}

    public LogicalLine getNextLogicalLine();
}
