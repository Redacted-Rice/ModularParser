package redactedrice.modularparser.log;


import redactedrice.modularparser.core.Supporter;

public interface LogSupporter extends Supporter {
    public enum LogLevel {
        DEBUG, INFO, WARN, ERROR
    };

    public void log(LogLevel level, String message);

    public void log(LogLevel level, String format, Object... args);
}
