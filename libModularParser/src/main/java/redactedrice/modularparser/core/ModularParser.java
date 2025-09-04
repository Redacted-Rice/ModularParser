package redactedrice.modularparser.core;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import redactedrice.modularparser.core.LogSupporter.LogLevel;

/**
 * A flexible Parser that can be configured to your needs and customized
 * with modules for specific syntax. This includes:
 * Line end (in progress)
 * Line breaks
 * Single line comments
 * Multi line comments
 * Modules for parsing instructions
 */
public class ModularParser {
    public enum Status {
        OK, ERROR, ABORT
    }

    protected Status status = Status.OK;

    protected LineFormerSupporter lineFormer = null;
    protected LineParserSupporter lineParser = null;
    protected LogSupporter logger = null;

    protected final List<Module> modulesOrdered = new ArrayList<>();
    protected final Map<String, Module> index = new HashMap<>();
    protected final Map<String, Supporter> supporters = new HashMap<>();

    // --------------- Configure parser Fns -----------------

    public boolean addModule(Module module) {
        // Check for name conflicts
        if (index.containsKey(module.getName())) {
            logOrStdErr("Module '" + module.getName() + "' already exists");
            return false;
        }

        module.setParser(this);

        if (!handleSpecialCaseAdds(module)) {
            return false;
        }

        // If its a supporter, keep track of it and pass it any existing modules
        if (module instanceof Supporter supporter) {
            String supporterName = getSupporterInterfaceName(supporter);
            if (supporters.putIfAbsent(supporterName, supporter) != null) {
                logOrStdErr("Attempted to add a second supporter for: "
                        + module.getClass().getCanonicalName());
                return false;
            }
            for (Module existing : modulesOrdered) {
                supporter.handleModule(existing);
            }
        }

        // Add this module finally
        index.put(module.getName(), module);
        modulesOrdered.add(module);
        return true;
    }

    protected boolean handleSpecialCaseAdds(Module module) {
        // See if its the log supporter
        if (module instanceof LogSupporter supporter) {
            if (logger != null) {
                // Unnecessary but seems cleaner to keep the pattern
                logOrStdErr("Attempted to add a second log supporter!");
                return false;
            }
            logger = supporter;
        }

        // Pass all to existing supporters
        for (Supporter supporter : supporters.values()) {
            supporter.handleModule(module);
        }

        // See if its one of our required supporters
        if (module instanceof LineFormerSupporter supporter) {
            if (lineFormer != null) {
                logOrStdErr("Attempted to add a second line former!");
                return false;
            }
            lineFormer = supporter;
        }
        if (module instanceof LineParserSupporter supporter) {
            if (lineParser != null) {
                logOrStdErr("Attempted to add a second line parser!");
                return false;
            }
            lineParser = supporter;
        }
        return true;
    }

    protected String getSupporterInterfaceName(Supporter supporter) {
        for (Class<?> iface : supporter.getClass().getInterfaces()) {
            if (Supporter.class.isAssignableFrom(iface) && !iface.equals(Supporter.class)) {
                return iface.getSimpleName(); // e.g. "AliasSupporter"
            }
        }
        logOrStdErr("No Sub Supporter interface of Supporter found");
        return "";
    }

    public boolean configureModules() {
        modulesOrdered.forEach(Module::setModuleRefs);
        List<String> failed = modulesOrdered.stream()
                .filter(module -> !module.checkModulesCompatibility()).map(Module::getName)
                .toList();
        if (!failed.isEmpty()) {
            logOrStdErr("The following modules are incompatibile with at least one other module. "
                    + "Check previous errors for details: " + String.join(", ", failed));
            return false;
        }
        return true;
    }

    // --------------- Main Parser Fns -----------------

    public boolean parse() {
        if (lineFormer == null) {
            logOrStdErr(LogLevel.ABORT, "ModularParser: No Line Former was added");
            notifyAbort();
        }

        if (lineParser == null) {
            logOrStdErr(LogLevel.ABORT, "ModularParser: No Line Parser was added");
            notifyAbort();
        }

        String line;
        while (!aborted() && (line = lineFormer.getNextLogicalLine()) != null) {
            if (line.isBlank()) {
                continue;
            }
            if (!lineParser.parseLine(line)) {
                logOrStdErr(LogLevel.ERROR,
                        "ModularParser: Unspecified error while parsing line %s", line);
            }
        }
        if (logger != null) {
            if (aborted()) {
                logOrStdErr("ModularParser: Aborted! See previous logs for details");
            } else if (getStatus() == Status.ERROR) {
                logOrStdErr(
                        "ModularParser: Failed to parser some lines! See previous logs for details");
            }
        }
        return status == Status.OK;
    }

    // ------------------ Logging/Status Fns -------------------

    protected void logOrStdErr(String format, Object... args) {
        logOrStdErr(LogLevel.ERROR, format, args);
    }

    protected void logOrStdErr(LogLevel level, String format, Object... args) {
        if (logger != null) {
            logger.log(level, String.format(format, args));
        } else {
            System.out.println(String.format(format, args)); // NOSONAR
        }
    }

    public void notifyError() {
        if (status.compareTo(Status.ERROR) < 0) {
            logOrStdErr("ModularParser: First Error Signaled");
            status = Status.ERROR;
        }
    }

    public void notifyAbort() {
        if (status.compareTo(Status.ABORT) < 0) {
            logOrStdErr(LogLevel.ERROR, "ModularParser: First Abort Signaled");
            status = Status.ABORT;
        }
    }

    public void resetStatus() {
        status = Status.OK;
    }

    public Status getStatus() {
        return status;
    }

    public boolean aborted() {
        return status == Status.ABORT;
    }

    // ------------------ Module Getters ----------------------
    public Module getModule(String name) {
        return index.get(name);
    }

    public <T> List<T> getModulesOfType(Class<T> clazz) {
        return modulesOrdered.stream().filter(clazz::isInstance).map(clazz::cast).toList();
    }

    public <T> T getSupporterOfType(Class<T> clazz) {
        // Should be only one
        Optional<Module> first = modulesOrdered.stream().filter(clazz::isInstance).findFirst();
        if (first.isEmpty()) {
            return null;
        }
        return clazz.cast(first.get());
    }

    public LogSupporter getLogger() {
        return logger;
    }
}