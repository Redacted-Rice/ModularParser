package redactedrice.modularparser.lineparser;


import redactedrice.modularparser.core.Module;

public interface LineParser extends Module {
    boolean tryParseLine(String logicalLine);
}
