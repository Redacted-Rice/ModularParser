package redactedrice.modularparser.core;


import redactedrice.modularparser.core.LogSupporter.LogLevel;

/** A named DSL‐line handler carrying a back‐pointer to its parser. */
public abstract class BaseModule implements Module {
    protected final String name;
    protected ModularParser parser;

    protected BaseModule(String name) {
        this.name = name;
    }

    /** The unique name you gave this handler. */
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setParser(ModularParser parser) {
        this.parser = parser;
    }

    @Override
    public void setModuleRefs() {}

    @Override
    public boolean checkModulesCompatibility() {
        return true;
    }

    public void log(LogLevel level, String message) {
        log(level, true, message);
    }

    public void log(LogLevel level, boolean notifyOnError, String message) {
        if (parser.getLogger() != null) {
            parser.getLogger().log(level, String.format("%s: %s", getName(), message));
        }
        if (notifyOnError) {
            if (level == LogLevel.ERROR) {
                parser.notifyError();
            } else if (level == LogLevel.ABORT) {
                parser.notifyAbort();
            }
        }
    }

    public void log(LogLevel level, String format, Object... args) {
        log(level, true, format, args);
    }

    public void log(LogLevel level, boolean notifyOnError, String format, Object... args) {
        if (parser.getLogger() != null) {
            log(level, notifyOnError, String.format(format, args));
        }
    }

    public void log(LogLevel level, Throwable error, String message) {
        log(level, true, error, message);
    }

    public void log(LogLevel level, boolean notifyOnError, Throwable error, String message) {
        if (parser.getLogger() != null) {
            log(level, notifyOnError, parser.getLogger().appendStackTrace(message, error));
        }
    }
}