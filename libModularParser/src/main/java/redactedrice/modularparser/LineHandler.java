package redactedrice.modularparser;


public interface LineHandler {
    boolean matches(String logicalLine);

    void handle(String logicalLine);
}
