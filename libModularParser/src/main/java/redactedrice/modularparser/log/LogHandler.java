package redactedrice.modularparser.log;


import redactedrice.modularparser.core.LogSupporter.LogLevel;
import redactedrice.modularparser.core.Module;

public interface LogHandler extends Module {
    void add(LogLevel level, String log);

    boolean enabled(LogLevel level);

    void enable(LogLevel level, boolean enabled);

    public static String defaultFormat(LogLevel level, String message) {
        return "[" + String.format("%-5s", level.name()) + "] " + message;
    }
}
