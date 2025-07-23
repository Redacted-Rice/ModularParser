package redactedrice.modularparser.log;


import redactedrice.modularparser.core.Module;
import redactedrice.modularparser.core.LogSupporter.LogLevel;

public interface LogHandler extends Module {
    void add(LogLevel level, String log);

    boolean enabled(LogLevel level);

    void enable(LogLevel level, boolean enabled);
}
