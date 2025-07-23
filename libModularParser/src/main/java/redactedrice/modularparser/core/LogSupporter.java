package redactedrice.modularparser.core;


public interface LogSupporter extends Supporter {
    public enum LogLevel {
        DEBUG, INFO, WARN, ERROR, ABORT
    };

    public void log(LogLevel level, String message);

    public String format(String format, Object... args);

    public String appendStackTrace(String message, Throwable error);
}
