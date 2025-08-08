package redactedrice.modularparser.lineparser;


import redactedrice.modularparser.core.BaseSupporter;
import redactedrice.modularparser.core.LineParserSupporter;
import redactedrice.modularparser.core.LogSupporter.LogLevel;

public class DefaultLineParserSupporter extends BaseSupporter<LineParser>
        implements LineParserSupporter {

    public DefaultLineParserSupporter() {
        super("LineParserSupportModule", LineParser.class);
    }

    @Override
    public boolean parseLine(String logicalLine) {
        // Route to the first matching Module
        for (LineParser handler : submodules) {
            if (handler.tryParseLine(logicalLine)) {
                return true;
            }
        }
        // Log an error if we failed to find one
        log(LogLevel.ERROR, "Unhandled Line: %s", logicalLine);
        return false;
    }
}
