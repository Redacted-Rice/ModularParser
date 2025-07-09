package redactedrice.modularparser;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redactedrice.modularparser.WordReserver.ReservedType;
import redactedrice.modularparser.lineformer.LineFormerSupporter;
import redactedrice.modularparser.scope.ScopeHandler;

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
    private final List<WordReserver> reservedWordModules = new ArrayList<>();
    private final List<ScopeHandler> scopeModules = new ArrayList<>();

    private LineFormerSupporter lineFormer = null;
    private final List<LineHandler> lineHandlerModules = new ArrayList<>();

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

        // Check for exclusive reserved-word conflicts:
        if (module instanceof WordReserver) {
            WordReserver asParserModule = (WordReserver) module;
            Set<String> exclusive = asParserModule.getReservedWords(ReservedType.EXCLUSIVE);
            for (WordReserver existing : reservedWordModules) {
                Map<String, ReservedType> common = new HashMap<>(existing.getAllReservedWords());
                common.keySet().retainAll(exclusive);
                if (!common.isEmpty()) {
                    // This should always be true but just in case have it here
                    if (existing instanceof Module) {
                        throw new IllegalArgumentException("Module '" + module.getName()
                                + "' exclusively reserves the following keys already reserved by '"
                                + ((Module) existing).getName() + "': " + common);
                    } else {
                        throw new IllegalArgumentException("Module '" + module.getName()
                                + "' and an unknown module both reserve: " + common);
                    }
                }
            }
            reservedWordModules.add(asParserModule);
        }

        index.put(module.getName(), module);

        // If its an alias replacer as well, kept track of it
        if (module instanceof LineHandler) {
            lineHandlerModules.add((LineHandler) module);
        }
        if (module instanceof ScopeHandler) {
            scopeModules.add((ScopeHandler) module);
        }

        if (module instanceof LineFormerSupporter) {
            if (lineFormer != null) {
                throw new RuntimeException("Attempted to add a second line former!");
            }
            lineFormer = (LineFormerSupporter) module;
        }
        if (module instanceof Supporter) {
            if (supporters.putIfAbsent(module.getClass().getCanonicalName(),
                    (Supporter) module) != null) {
                throw new RuntimeException("Attempted to add a second supporter for: "
                        + module.getClass().getCanonicalName());
            }
        }
        modulesOrdered.add(module);
    }

    public void configureModules() {
        modulesOrdered.forEach(module -> module.configure());
    }

    // --------------- Main Parser Fns -----------------

    public void parse() {
        String line;
        while ((line = lineFormer.getNextLogicalLine()) != null) {
            dispatch(line);
        }
    }

    private void dispatch(String logicalLine) {
        // Now route to the first matching Module
        for (LineHandler h : lineHandlerModules) {
            if (h.matches(logicalLine)) {
                h.handle(logicalLine);
                return;
            }
        }
        System.err.println("UNHANDLED → " + logicalLine);
    }

    // ------------------ Getters ----------------------

    public Map<String, ReservedType> getAllReservedWords() {
        Map<String, ReservedType> all = new HashMap<>();
        for (WordReserver h : reservedWordModules) {
            all.putAll(h.getAllReservedWords());
        }
        return all;
    }

    public Set<String> getReservedWords(ReservedType type) {
        Set<String> all = new HashSet<>();
        for (WordReserver h : reservedWordModules) {
            all.addAll(h.getReservedWords(type));
        }
        return all;
    }

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

    public ScopeHandler getScoperFor(String module) {
        for (ScopeHandler scope : scopeModules) {
            if (scope.handlesModule(module)) {
                return scope;
            }
        }
        return null;
    }
}