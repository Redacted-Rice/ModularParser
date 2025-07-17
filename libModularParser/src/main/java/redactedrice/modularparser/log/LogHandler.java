package redactedrice.modularparser.log;


import redactedrice.modularparser.core.Module;

public interface LogHandler extends Module {
    void add(String log);
}
