package redactedrice.modularparser.log;


import redactedrice.modularparser.core.BaseModule;
import redactedrice.modularparser.log.LogSupporter.LogLevel;

public class BaseLoggingModule extends BaseModule {
    protected LogSupporter logger;

    protected BaseLoggingModule(String name) {
        super(name);
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
