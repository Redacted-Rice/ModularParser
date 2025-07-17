package redactedrice.modularparser.log;


import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import redactedrice.modularparser.core.BaseModule;
import redactedrice.modularparser.core.Module;

public class DefaultLogSupporter extends BaseModule implements LogSupporter {
    protected final List<LogHandler> handlers = new ArrayList<>();
    protected final Set<LogLevel> enabledLevels;

    public DefaultLogSupporter() {
        super("DefaultLogSupporter");
        this.enabledLevels = EnumSet.noneOf(LogLevel.class);
    }

    @Override
    public void handleModule(Module module) {
        if (module instanceof LogHandler) {
            handlers.add((LogHandler) module);
        }
    }

    @Override
    public void log(LogLevel level, String message) {
        if (isEnabled(level)) {
            System.out.println(format(level, message));
        }
    }

    @Override
    public void log(LogLevel level, String format, Object... args) {
        if (isEnabled(level)) {
            String msg = String.format(format, args);
            System.out.println(format(level, msg));
        }
    }

    @Override
    public void log(LogLevel level, String message, Throwable throwable) {
        if (isEnabled(level)) {
            System.out.println(format(level, message));
            throwable.printStackTrace(System.out);
        }
    }

    public boolean isEnabled(LogLevel level) {
        return enabledLevels.contains(level);
    }

    private String format(LogLevel level, String message) {
        return "[" + level.name() + "] " + message;
    }
}
