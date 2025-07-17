package redactedrice.modularparser.core;


public interface LineFormerSupporter extends Supporter {
    public record LineRange(int first, int last) {}

    public String getNextLogicalLine();

    public LineRange getCurrentLineRange();
}
