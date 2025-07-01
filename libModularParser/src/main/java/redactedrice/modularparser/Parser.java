package redactedrice.modularparser;


import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

/**
 * A flexible Parser that can be configured to your needs and customized
 * with modules for specific syntax. This includes:
 *   Line end (in progress)
 *   Line breaks
 *   Single line comments
 *   Multi line comments
 *   Modules for parsing instructions
 */
public class Parser {
    private final Set<String> lineContinue = new HashSet<>();
    private final Set<String> singleLineComment = new HashSet<>();
    private final Map<String, String> multiLineComment = new HashMap<>();

    private final List<LiteralModule> literalModules = new ArrayList<>();
    private final List<AliasModule> aliasModules = new ArrayList<>();
    private final List<VariableModule> variableModules = new ArrayList<>();
    private final List<LineHandlerModule> lineHandlerModules = new ArrayList<>();
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
        
        // Check for reserved-word conflicts:
        if (module instanceof LineHandlerModule) {
        	LineHandlerModule asParserModule = (LineHandlerModule) module;
	        Set<String> newRes = asParserModule.getReservedWords();
	        for (LineHandlerModule existing : lineHandlerModules) {
	            Set<String> common = new HashSet<>(existing.getReservedWords());
	            common.retainAll(newRes);
	            if (!common.isEmpty()) {
	                throw new IllegalArgumentException("Module '" + module.getName() +
	                                   "' and module '" + existing.getName() +
	                                   "' both reserve " + common);
	            }
	        }
	        lineHandlerModules.add(asParserModule);
        }
        
        module.setParser(this);
        index.put(module.getName(), module);
        
        // If its an alias replacer as well, kept track of it
        if (module instanceof LiteralModule) {
        	literalModules.add((LiteralModule)module);
        }
        if (module instanceof AliasModule) {
        	aliasModules.add((AliasModule)module);
        }
        if (module instanceof VariableModule) {
        	variableModules.add((VariableModule)module);
        }
    }

    // --------------- Main Parser Fns ----------------- 
    
    // TODO Remove the comment and keep parsing around it
    // TODO comments don't have to start the line - anything past/between is ignored
    
    // TODO replace Buffered Reader for newline flexibility?
    public void parse(BufferedReader in) throws IOException {
        String raw;
        while ((raw = in.readLine()) != null) {
        	raw = raw.trim();
            if (startsWith(raw, singleLineComment)) {
            	// System.out.println("Skipping comment: " + raw);
            	continue;
            }
            
            String commentEnd = startsWith(raw, multiLineComment);
            if (!commentEnd.isEmpty()) {
            	// System.out.println("MultilineComment: " + raw);
                // consume up to closing */
                accumulateComment(raw, in, commentEnd);
            } else {
            	//System.out.println("Non comment: " + raw);
                // consume nested-parens & continuers
            	String logical = accumulate(raw, in);
                dispatch(logical);
            }
        }
    }

    // Read until we find the end of the multiline comment
    private String accumulateComment(String firstLine, BufferedReader in, String commentEnd) throws IOException {
        StringBuilder sb = new StringBuilder(firstLine);
        String line = firstLine;
        
        while (!line.contains(commentEnd)) {
            line = in.readLine();
            if (line == null) {
            	break;
            }
            sb.append(" ");
            sb.append(line.trim());
        }
        return sb.toString();
    }

    // Merge lines until outer () balance is zero and no continuer
    private String accumulate(String firstLine, BufferedReader in) throws IOException {
        StringBuilder sb = new StringBuilder(firstLine);
        int parenDepth = determineParenthesisDelta(firstLine);
        boolean needsMore = parenDepth > 0 || endsWith(firstLine, lineContinue);

        while (needsMore) {
            String next = in.readLine();
            if (next == null) {
            	break;
            }
            System.out.println("'" + next.trim() + "'");
            sb.append(" ");
            sb.append(next.trim());
            parenDepth += determineParenthesisDelta(next);
            needsMore  = parenDepth > 0 || endsWith(next, lineContinue);
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

    private boolean startsWith(String line, Set<String> tokens) {
        for (String token : tokens) {
            if (line.startsWith(token)) {
            	return true;
            }
        }
        return false;
    }
    
    private String startsWith(String line, Map<String, String> tokens) {
        for (Entry<String, String> token : tokens.entrySet()) {
            if (line.startsWith(token.getKey())) {
            	return token.getValue();
            }
        }
        return "";
    }

    private boolean endsWith(String line, Set<String> tokens) {
        for (String token : tokens) {
            if (line.endsWith(token)) {
            	return true;
            }
        }
        return false;
    }

    private void dispatch(String logicalLine) {
      // Apply any alias‐substitutions
      for (AliasModule aliaser : aliasModules) {
          logicalLine = aliaser.replaceAliases(logicalLine);
      }
      
      // Now route to the first matching Module
      for (LineHandlerModule h : lineHandlerModules) {
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
        for (LiteralModule literalModule : literalModules) {
        	ret = literalModule.tryEvaluateLiteral(literal);
        	if (ret.isPresent()) {
        		return ret.get();
        	}
        }
        return null;
    }
    
    public boolean isAliasDefined(String alias) {
        for (AliasModule aliasModule : aliasModules) {
            if (aliasModule.isAlias(alias)) {
            	return true;
            }
        }
        return false;
    }
    
    public boolean isVariableDefined(String var) {
        for (VariableModule variableModule : variableModules) {
            if (variableModule.isVariable(var)) {
            	return true;
            }
        }
        return false;
    }
    
    // ------------------ Getters ----------------------
    
    // TODO make this a hashset with type as value?
    public Set<String> getAllReservedWords() {
        Set<String> all = new HashSet<>();
        for (LineHandlerModule h : lineHandlerModules) {
            all.addAll(h.getReservedWords());
        }
        return all;
    }
    
    public Set<String> getAllAliases() {
        Set<String> all = new HashSet<>();
        for (AliasModule aliaser : aliasModules) {
            all.addAll(aliaser.getReservedWords());
        }
        return all;
    }
    
    public Set<String> getAllVariables() {
        Set<String> all = new HashSet<>();
        for (VariableModule varModule : variableModules) {
            all.addAll(varModule.getReservedWords());
        }
        return all;
    }
    
    public Module getModule(String name) {
        return index.get(name);
    }
    
    public List<AliasModule> getAliasModules(String name) {
        return aliasModules;
    }
}