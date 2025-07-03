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
import java.util.Optional;
import java.util.Set;

import redactedrice.modularparser.WordReserver.ReservedType;

/**
 * A flexible Parser that can be configured to your needs and customized
 * with modules for specific syntax. This includes:
 * Line end (in progress)
 * Line breaks
 * Single line comments
 * Multi line comments
 * Modules for parsing instructions
 */
public class Parser {
    private final Set<String> lineContinue = new HashSet<>();
    private final Set<String> singleLineComment = new HashSet<>();
    private final Map<String, String> multiLineComment = new HashMap<>();

    private final List<LiteralHandler> literalModules = new ArrayList<>();
    private final List<AliasHandler> aliasModules = new ArrayList<>();
    private final List<VariableHandler> variableModules = new ArrayList<>();
    private final List<WordReserver> reservedWordModules = new ArrayList<>();
    private final List<LineHandler> lineHandlerModules = new ArrayList<>();
    private final Map<String, Module> index = new HashMap<>();

    // --------------- Configure parser Fns -----------------
    public void addLineContinue(String token) {
        lineContinue.add(token);
    }

    public void addSingleLineComment(String token) {
        singleLineComment.add(token);
    }

    public void addMultiLineComment(String startToken, String endToken) {
        multiLineComment.put(startToken, endToken);
    }

    public void addModule(Module module) {
        // Check for name conflicts
        if (index.containsKey(module.getName())) {
            throw new IllegalArgumentException("Module '" + module.getName() + "' already exists");
        }

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

        module.setParser(this);
        index.put(module.getName(), module);

        // If its an alias replacer as well, kept track of it
        if (module instanceof LineHandler) {
            lineHandlerModules.add((LineHandler) module);
        }
        if (module instanceof LiteralHandler) {
            literalModules.add((LiteralHandler) module);
        }
        if (module instanceof AliasHandler) {
            aliasModules.add((AliasHandler) module);
        }
        if (module instanceof VariableHandler) {
            variableModules.add((VariableHandler) module);
        }
    }

    // --------------- Main Parser Fns -----------------

    // TODO Remove the comment and keep parsing around it
    // TODO comments don't have to start the line - anything past/between is ignored

    // TODO replace Buffered Reader for newline flexibility?
    public void parse(BufferedReader in) throws IOException {
        String raw;
        while ((raw = in.readLine()) != null) {
            raw = removeAnyComments(raw, in);
            if (raw.isBlank()) {
                continue;
            }

            // System.out.println("Non comment: " + raw);
            // consume nested-parens & continuers
            String logical = accumulate(raw, in);
            dispatch(logical);
        }
    }

    // Merge lines until outer () balance is zero and no continuer
    private String accumulate(String firstLine, BufferedReader in) throws IOException {
        String stripped = endsWith(firstLine, lineContinue);
        if (!stripped.isEmpty()) {
            firstLine = stripped.trim();
        } else {
            firstLine = firstLine.trim();
        }

        StringBuilder sb = new StringBuilder(firstLine);
        int parenDepth = determineParenthesisDelta(firstLine);

        while (parenDepth > 0 || !stripped.isEmpty()) {
            String next = in.readLine();
            if (next == null) {
                throw new MissingFormatArgumentException(
                        "Reach end of file while parsing parenthesis");
            }
            next = removeAnyComments(next, in);
            stripped = endsWith(next, lineContinue);
            if (!stripped.isEmpty()) {
                next = stripped.trim();
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

    private String endsWith(String line, Set<String> tokens) {
        for (String token : tokens) {
            if (line.endsWith(token)) {
                return line.substring(0, line.length() - token.length());
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

    public Object evaluateLiteral(String literal) {
        Optional<Object> ret;
        for (LiteralHandler literalModule : literalModules) {
            ret = literalModule.tryEvaluateLiteral(literal);
            if (ret.isPresent()) {
                return ret.get();
            }
        }
        return null;
    }

    public boolean isAliasDefined(String alias) {
        for (AliasHandler aliasModule : aliasModules) {
            if (aliasModule.isAlias(alias)) {
                return true;
            }
        }
        return false;
    }

    public boolean isVariableDefined(String var) {
        for (VariableHandler variableModule : variableModules) {
            if (variableModule.isVariable(var)) {
                return true;
            }
        }
        return false;
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

    public Set<String> getAllAliases() {
        Set<String> all = new HashSet<>();
        for (AliasHandler aliaser : aliasModules) {
            all.addAll(aliaser.getAliases());
        }
        return all;
    }

    public Map<String, Object> getAllVariables() {
        Map<String, Object> all = new HashMap<>();
        for (VariableHandler varModule : variableModules) {
            all.putAll(varModule.getVariables());
        }
        return all;
    }

    public Module getModule(String name) {
        return index.get(name);
    }

    public List<AliasHandler> getAliasModules(String name) {
        return aliasModules;
    }
}