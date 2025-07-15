package redactedrice.modularparser.lineparser;

import redactedrice.modularparser.core.BaseSupporter;
import redactedrice.modularparser.core.LineParserSupporter;

public class DefaultLineParserSupport extends BaseSupporter<LineParser> implements LineParserSupporter {

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
        System.err.println("UNHANDLED → " + logicalLine);
	}
}
