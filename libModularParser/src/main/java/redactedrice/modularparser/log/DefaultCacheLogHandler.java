package redactedrice.modularparser.log;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import redactedrice.modularparser.core.BaseModule;
import redactedrice.modularparser.core.LogSupporter.LogLevel;

public class DefaultCacheLogHandler extends BaseModule implements LogHandler {
    // Stores log entries in insertion order
    private final Queue<String> logs = new ConcurrentLinkedQueue<>();
    // Tracks which levels are enabled
    private final Map<LogLevel, Boolean> enabledLevels = new ConcurrentHashMap<>();

    public DefaultCacheLogHandler() {
        super(DefaultCacheLogHandler.class.getSimpleName());
        // Default: enable all levels
        for (LogLevel level : LogLevel.values()) {
            enabledLevels.put(level, true);
        }
    }

    @Override
    public void add(LogLevel level, String log) {
        if (enabled(level)) {
            logs.add(LogHandler.defaultFormat(level, log));
        }
    }

    @Override
    public boolean enabled(LogLevel level) {
        return enabledLevels.getOrDefault(level, false);
    }

    @Override
    public void enable(LogLevel level, boolean enabled) {
        enabledLevels.put(level, enabled);
    }

    public List<String> getLogs() {
        return new ArrayList<>(logs);
    }

    public void clear() {
        logs.clear();
    }
}
