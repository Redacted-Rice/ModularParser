package redactedrice.modularparser.lineparser;

import redactedrice.modularparser.core.LineParserSupporter;
import redactedrice.modularparser.log.BaseLoggingSupporter;
import redactedrice.modularparser.log.LogSupporter.LogLevel;

public class DefaultLineParserSupport extends BaseLoggingSupporter<LineParser> implements LineParserSupporter {

	public DefaultLineParserSupport() {
		super("LineParserSupportModule", LineParser.class);
	}

	@Override
	public void parseLine(String logicalLine) {
		// Now route to the first matching Module
		for (LineParser handler : submodules) {
			if (handler.tryParseLine(logicalLine)) {
                return;
            }
        }
		log(LogLevel.ERROR, "Unhandled Line: %s", logicalLine);
	}
}
