package redactedrice.modularparser.log;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import redactedrice.modularparser.core.BaseModule;
import redactedrice.modularparser.core.LogSupporter;
import redactedrice.modularparser.core.Module;

public class DefaultLogSupporter extends BaseModule implements LogSupporter {
    protected final List<LogHandler> handlers = new ArrayList<>();

    public DefaultLogSupporter() {
        super(DefaultLogSupporter.class.getSimpleName());
    }

    @Override
    public void handleModule(Module module) {
        if (module instanceof LogHandler handler) {
            handlers.add(handler);
        }
    }

    @Override
    public void log(LogLevel level, String message) {
        for (LogHandler handler : handlers) {
            handler.add(level, message);
        }
    }

    @Override
    public String appendStackTrace(String message, Throwable error) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        error.printStackTrace(pw);
        return message + "\nStack Trace:\n" + sw.toString();
    }
}
