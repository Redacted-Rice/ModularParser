package redactedrice.modularparser.core;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	public enum Status { OK, ERROR, ABORT};
	
	protected Status status = Status.OK;
	
	protected LineFormerSupporter lineFormer = null;
	protected LineParserSupporter lineParser = null;
	protected LogSupporter logger = null;

    protected final List<Module> modulesOrdered = new ArrayList<>();
    protected final Map<String, Module> index = new HashMap<>();
    protected final Map<String, Supporter> supporters = new HashMap<>();

    // --------------- Configure parser Fns -----------------

    public void addModule(Module module) {
        // Check for name conflicts
        if (index.containsKey(module.getName())) {
            throw new IllegalArgumentException("Module '" + module.getName() + "' already exists");
        }

        module.setParser(this);
        
        // See if its the log supporter
        if (module instanceof LogSupporter) {
            if (logger != null) {
                throw new RuntimeException("Attempted to add a second line former!");
            }
            logger = (LogSupporter) module;
        }

        // Pass all to existing supporters
        for (Supporter supporter : supporters.values()) {
            supporter.handleModule(module);
        }

        // See if its one of our required supporters
        if (module instanceof LineFormerSupporter) {
            if (lineFormer != null) {
                throw new RuntimeException("Attempted to add a second line former!");
            }
            lineFormer = (LineFormerSupporter) module;
        }
        if (module instanceof LineParserSupporter) {
            if (lineParser != null) {
                throw new RuntimeException("Attempted to add a second line parser!");
            }
            lineParser = (LineParserSupporter) module;
        }

        // If its a supporter, keep track of it and pass it any existing modules
        if (module instanceof Supporter) {
            Supporter asSupporter = (Supporter) module;
            String supporterName = getSupporterInterfaceName(asSupporter);
            if (supporters.putIfAbsent(supporterName, asSupporter) != null) {
                throw new RuntimeException("Attempted to add a second supporter for: "
                        + module.getClass().getCanonicalName());
            }
            for (Module existing : modulesOrdered) {
                asSupporter.handleModule(existing);
            }
        }

        // Add this module finally
        index.put(module.getName(), module);
        modulesOrdered.add(module);
    }

    protected static String getSupporterInterfaceName(Supporter supporter) {
        for (Class<?> iface : supporter.getClass().getInterfaces()) {
            if (Supporter.class.isAssignableFrom(iface) && !iface.equals(Supporter.class)) {
                return iface.getSimpleName(); // e.g. "AliasSupporter"
            }
        }
        throw new IllegalArgumentException("No Sub Supporter interface of Supporter found");
    }

    public void configureModules() {
        modulesOrdered.forEach(module -> module.setModuleRefs());
        List<String> failed = modulesOrdered.stream()
                .filter(module -> !module.checkModulesCompatibility()).map(Module::getName)
                .toList();
        if (failed.size() > 0) {
            throw new IllegalArgumentException(
                    "The following modules are incompatibile with at least one other module. Check previous errors for details: "
                            + String.join(", ", failed));
        }
    }

    // --------------- Main Parser Fns -----------------

    public void parse() {
        String line;
        while (!aborted() && (line = lineFormer.getNextLogicalLine()) != null) {
            lineParser.parseLine(line);
        }
    	if (logger != null) {
	        if (aborted()) {
	        	logger.log(LogLevel.ERROR, "ModularParser: Aborted! See previous logs for details");
	        } else if (getStatus() == Status.ERROR) {
	        	logger.log(LogLevel.ERROR, "ModularParser: Failed to parser some lines! See previous logs for details");
	        }
    	}
    }

    // ------------------ Status Fns -------------------
    
    public void notifyError() {
    	if (status.compareTo(Status.ERROR) < 0) {
    		if (logger != null) {
    			logger.log(LogLevel.ERROR, "ModularParser: First Error Signaled");
    		}
    		status = Status.ERROR;
    	}
    }
    
    public void notifyAbort() {
    	if (status.compareTo(Status.ABORT) < 0) {
    		if (logger != null) {
    			logger.log(LogLevel.ERROR, "ModularParser: First Abort Signaled");
    		}
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
        return modulesOrdered.stream().filter(clazz::isInstance).findFirst().map(clazz::cast).get();
    }
    
    public LogSupporter getLogger() {
		return logger;
    }
}