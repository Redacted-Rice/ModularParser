package redactedrice.modularparser;


import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingFormatArgumentException;
import java.util.Set;

import redactedrice.modularparser.WordReserver.ReservedType;
import redactedrice.modularparser.alias.AliasHandler;
import redactedrice.modularparser.scope.ScopeHandler;
import redactedrice.modularparser.variable.VariableHandler;

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
    private final Map<String, Boolean> lineContinue = new HashMap<>();
    private final Set<String> singleLineComment = new HashSet<>();
    private final Map<String, String> multiLineComment = new HashMap<>();

    private final List<WordReserver> reservedWordModules = new ArrayList<>();
    private final List<LineHandler> lineHandlerModules = new ArrayList<>();
    private final List<ScopeHandler> scopeModules = new ArrayList<>();
    private final List<Module> modulesOrdered = new ArrayList<>();
    private final Map<String, Module> index = new HashMap<>();
    private final Map<String, Supporter> supporters = new HashMap<>();

    // --------------- Configure parser Fns -----------------
    public void addLineContinue(String token, boolean removeToken) {
        lineContinue.put(token, removeToken);
    }

    public void addSingleLineComment(String token) {
        singleLineComment.add(token);
    }

    public void addMultiLineComment(String startToken, String endToken) {
        multiLineComment.put(startToken, endToken);
    }
    
    public void addSupporter(Supporter supporter) {
    }

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
        if (module instanceof Supporter) {
        	supporters.put(module.getClass().getCanonicalName(), (Supporter) module);
        }
        modulesOrdered.add(module);
    }

    public void configureModules() {
        modulesOrdered.forEach(module -> module.configure());
    }

    // --------------- Main Parser Fns -----------------

    // TODO replace Buffered Reader for newline flexibility?
    public void parse(BufferedReader in) throws IOException {
        String raw;
        while ((raw = in.readLine()) != null) {
            raw = removeAnyComments(raw, in);
            if (raw.isBlank()) {
                continue;
            }

            // consume nested-parens & continuers
            String logical = accumulate(raw, in);
            dispatch(logical);
        }
    }

    // TODO: Handle line continuer/chainer inside quotes

    // Merge lines until outer () balance is zero and no continuer
    private String accumulate(String firstLine, BufferedReader in) throws IOException {
        String continuedLine = handleContination(firstLine);
        if (!continuedLine.isEmpty()) { // i.e. there is a continuation
            firstLine = continuedLine.trim();
        } else { // no continuation
            firstLine = firstLine.trim();
        }

        StringBuilder sb = new StringBuilder(firstLine);
        int parenDepth = determineParenthesisDelta(firstLine);

        while (parenDepth > 0 || !continuedLine.isEmpty()) {
            String next = in.readLine();
            if (next == null) {
                throw new MissingFormatArgumentException(
                        "Reach end of file while parsing parenthesis");
            }
            next = removeAnyComments(next, in);
            continuedLine = handleContination(next);
            if (!continuedLine.isEmpty()) {
                next = continuedLine.trim();
            } else {
                next = next.trim();
            }

            // System.out.println("'" + next + "'");
            if (!next.isBlank()) {
                sb.append(" ");
                sb.append(next);
                parenDepth += determineParenthesisDelta(next);
            }
        }
        return sb.toString();
    }

    // +1 for '('; –1 for ')'
    private int determineParenthesisDelta(String line) {
        int d = 0;
        for (char c : line.toCharArray()) {
            if (c == '(') {
                d++;
            } else if (c == ')') {
                d--;
            }
        }
        return d;
    }

    private String removeAnyComments(String raw, BufferedReader in) throws IOException {
        raw = removeAnySingleLineComment(raw.trim());
        if (raw.isBlank()) {
            return "";
        }

        raw = removeAnyMultiLineComment(raw, in);
        return raw;
    }

    // Read until we find the end of the multiline comment
    private String removeAnyMultiLineComment(String firstLine, BufferedReader in)
            throws IOException {
        String startToken = "";
        String endToken = "";
        int firstIdx = -1;
        int idx = -1;
        // Do we have a start token?
        for (Entry<String, String> token : multiLineComment.entrySet()) {
            idx = firstLine.indexOf(token.getKey());
            if (idx >= 0) {
                if (firstIdx < 0 || idx < firstIdx) {
                    firstIdx = idx;
                    startToken = token.getKey();
                    endToken = token.getValue();
                }
            }
        }
        if (firstIdx < 0) {
            return firstLine;
        }

        // Add the start of the line
        StringBuilder sb = new StringBuilder(firstLine.substring(0, firstIdx).trim());
        String line = firstLine;

        // Continue grabbing lines till we find the end
        int endIdx = line.indexOf(endToken);
        while (endIdx < 0) {
            line = in.readLine();
            if (line == null) {
                throw new MissingFormatArgumentException(
                        "Reach end of file while parsing multiline comment starting with "
                                + startToken + " and end of " + endToken);
            }
            endIdx = line.indexOf(endToken);
            // No need to append - its just thrown away
        }
        // Append any trailing line of the comment with a space between
        sb.append(' ');
        sb.append(line.substring(endIdx + endToken.length()).trim());
        return sb.toString();
    }

    private String removeAnySingleLineComment(String line) {
        int idx;
        for (String token : singleLineComment) {
            idx = line.indexOf(token);
            if (idx >= 0) {
                line = line.substring(0, idx);
            }
        }
        return line.trim();
    }

    private String handleContination(String line) {
        for (Entry<String, Boolean> entry : lineContinue.entrySet()) {
            if (line.endsWith(entry.getKey())) {
                if (entry.getValue()) {
                    return line.substring(0, line.length() - entry.getKey().length());
                } else {
                    return line;
                }
            }
        }
        return "";
    }

    private void dispatch(String logicalLine) {
        // Apply any alias‐substitutions
        for (AliasHandler aliaser : aliasModules) {
            logicalLine = aliaser.replaceAliases(logicalLine);
        }

        // Now route to the first matching Module
        for (LineHandler h : lineHandlerModules) {
            if (h.matches(logicalLine)) {
                h.handle(logicalLine);
                return;
            }
        }
        System.err.println("UNHANDLED → " + logicalLine);
    }

    // ------------- Public Fns for Modules ------------

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
        return modulesOrdered.stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .toList();
    }

    public <T> T getSupporterOfType(Class<T> clazz) {
        return modulesOrdered.stream()
                .filter(clazz::isInstance)
                .findFirst()
                .map(clazz::cast)
                .get();
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