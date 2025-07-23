package redactedrice.modularparser.log;


import redactedrice.modularparser.core.Supporter;

public interface LogSupporter extends Supporter {
    public enum LogLevel {
        DEBUG, INFO, WARN, ERROR
    };

    public void log(LogLevel level, String message);
    
    public String format(String format, Object... args);
    
    public String appendStackTrace(String message, Throwable error);
}
