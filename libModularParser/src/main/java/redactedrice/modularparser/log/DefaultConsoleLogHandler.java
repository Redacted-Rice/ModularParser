package redactedrice.modularparser.log;


import java.util.EnumSet;
import java.util.Set;

import redactedrice.modularparser.core.BaseModule;
import redactedrice.modularparser.core.LogSupporter.LogLevel;

public class DefaultConsoleLogHandler extends BaseModule implements LogHandler {
    protected final Set<LogLevel> enabledLevels;

    public DefaultConsoleLogHandler() {
        super(DefaultConsoleLogHandler.class.getSimpleName());
        this.enabledLevels = EnumSet.allOf(LogLevel.class);
    }

    public DefaultConsoleLogHandler(Set<LogLevel> enabledLevels) {
        super("DefaultConsoleLogSupporter");
        this.enabledLevels = EnumSet.copyOf(enabledLevels);
    }

    @Override
    public void add(LogLevel level, String log) {
        if (enabled(level)) {
            if (level.ordinal() >= LogLevel.ERROR.ordinal()) {
                System.err.println(LogHandler.defaultFormat(level, log)); // NOSONAR
            } else {
                System.out.println(LogHandler.defaultFormat(level, log)); // NOSONAR
            }
        }
    }

    @Override
    public boolean enabled(LogLevel level) {
        return enabledLevels.contains(level);
    }

    @Override
    public void enable(LogLevel level, boolean enabled) {
        if (enabled) {
            enabledLevels.add(level);
        } else {
            enabledLevels.remove(level);
        }
    }
}
