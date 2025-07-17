package redactedrice.modularparser.log;


import java.util.EnumSet;
import java.util.Set;

import redactedrice.modularparser.core.BaseSupporter;
import redactedrice.modularparser.core.Module;

public class DefaultConsoleLogHandler extends BaseSupporter<LogHandler> implements LogSupporter {
    protected final Set<LogLevel> enabledLevels;

    public DefaultConsoleLogHandler(Set<LogLevel> enabledLevels) {
        super("DefaultConsoleLogSupporter", LogHandler.class);
        this.enabledLevels = EnumSet.copyOf(enabledLevels);
    }

    @Override
    public void handleModule(Module module) {}

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
