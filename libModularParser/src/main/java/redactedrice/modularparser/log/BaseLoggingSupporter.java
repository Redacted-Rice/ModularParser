package redactedrice.modularparser.log;

import redactedrice.modularparser.core.BaseSupporter;
import redactedrice.modularparser.log.LogSupporter.LogLevel;

public class BaseLoggingSupporter<T> extends BaseSupporter<T> {
    protected LogSupporter logger;

	protected BaseLoggingSupporter(String name, Class<T> tClass) {
		super(name, tClass);
	}

    @Override
    public void setModuleRefs() {
    	super.setModuleRefs();
        logger = parser.getSupporterOfType(LogSupporter.class);
    }

    public void log(LogLevel level, String message) {
    	logger.log(level, logger.format("%s: %s", getName(), message));
    }

    public void log(LogLevel level, String format, Object... args) {
    	String message = logger.format(format, args);
    	log(level, message);
    }
    
    public void log(LogLevel level, String message, Throwable error) {
    	logger.log(level, logger.appendStackTrace(message, error));
    }
}
