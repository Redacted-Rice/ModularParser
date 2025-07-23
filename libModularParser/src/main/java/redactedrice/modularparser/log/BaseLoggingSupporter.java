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
    	logger.log(level, "%s: %s", getName(), message);
    }

    public void log(LogLevel level, String format, Object... args) {
        Object[] fullArgs = new Object[args.length + 1];
        fullArgs[0] = getName();
        System.arraycopy(args, 0, fullArgs, 1, args.length);

    	logger.log(level, "%s: " + format, fullArgs);
    }
}
