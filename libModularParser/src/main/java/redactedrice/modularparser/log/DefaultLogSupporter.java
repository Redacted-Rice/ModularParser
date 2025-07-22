package redactedrice.modularparser.log;


import java.util.ArrayList;
import java.util.List;

import redactedrice.modularparser.core.BaseModule;
import redactedrice.modularparser.core.Module;

public class DefaultLogSupporter extends BaseModule implements LogSupporter {
    protected final List<LogHandler> handlers = new ArrayList<>();

    public DefaultLogSupporter() {
        super("DefaultLogSupporter");
    }

    @Override
    public void handleModule(Module module) {
        if (module instanceof LogHandler) {
            LogHandler asHandler = (LogHandler) module;
            handlers.add(asHandler);
        }
    }

    @Override
    public void log(LogLevel level, String message) {
        for (LogHandler handler : handlers) {
            handler.add(level, message);
        }
    }

    @Override
    public void log(LogLevel level, String format, Object... args) {
        String msg = String.format(format, args);
        log(level, msg);
    }
}
