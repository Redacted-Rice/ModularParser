package redactedrice.modularparser.core;


public interface LineFormerSupporter extends Supporter {
    public record LineRange(int start, int end) {}

    public String getNextLogicalLine();

    public LineRange getCurrentLineRange();
}
