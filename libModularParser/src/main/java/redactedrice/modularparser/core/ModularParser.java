package redactedrice.modularparser.core;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private LineFormerSupporter lineFormer = null;
    private LineParserSupporter lineParser = null;

    private final List<Module> modulesOrdered = new ArrayList<>();
    private final Map<String, Module> index = new HashMap<>();
    // TODO: Need to work on how to store/access these
    private final Map<String, Supporter> supporters = new HashMap<>();

    // --------------- Configure parser Fns -----------------

    public void addModule(Module module) {
        // Check for name conflicts
        if (index.containsKey(module.getName())) {
            throw new IllegalArgumentException("Module '" + module.getName() + "' already exists");
        }

        module.setParser(this);

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
        modulesOrdered.forEach(module -> module.configure());
    }

    // --------------- Main Parser Fns -----------------

    public void parse() {
        String line;
        while ((line = lineFormer.getNextLogicalLine()) != null) {
            lineParser.parseLine(line);
        }
    }

    // ------------------ Getters ----------------------
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
}