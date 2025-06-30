package redactedrice.modularparser;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

/**
 * A flexible Parser that can be configured to your needs and customized
 * with handlers for specific syntax. This includes:
 *   Line end (in progress)
 *   Line breaks
 *   Single line comments
 *   Multi line comments
 *   Handlers for parsing instructions
 */
public class Parser {
    private final Set<String> lineContinue = new HashSet<>();
    private final Set<String> singleLineComment = new HashSet<>();
    private final Map<String, String> multiLineComment = new HashMap<>();

    private final List<AliasModule> aliasHandlers = new ArrayList<>();
    private final List<VariableModule> variableHandlers = new ArrayList<>();
    private final List<ParserModule> handlers = new ArrayList<>();
    private final Map<String, ParserModule> index = new HashMap<>();

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

    public void addHandler(ParserModule handler) {
    	// Check for name conflicts
        if (index.containsKey(handler.getName())) {
            throw new IllegalArgumentException("Handler '" + handler.getName() + "' already exists");
        }
        
        // Check for reserved-word conflicts:
        Set<String> newRes = handler.getReservedWords();
        for (ParserModule existing : handlers) {
            Set<String> common = new HashSet<>(existing.getReservedWords());
            common.retainAll(newRes);
            if (!common.isEmpty()) {
                throw new IllegalArgumentException("Handler '" + handler.getName() +
                                   "' and handler '" + existing.getName() +
                                   "' both reserve " + common);
            }
        }
        
        handler.setParser(this);
        handlers.add(handler);
        index.put(handler.getName(), handler);
        
        // If its an alias replacer as well, kept track of it
        if (handler instanceof AliasModule) {
        	aliasHandlers.add((AliasModule)handler);
        }
        if (handler instanceof VariableModule) {
        	variableHandlers.add((VariableModule)handler);
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
      for (AliasModule aliaser : aliasHandlers) {
          logicalLine = aliaser.replaceAliases(logicalLine);
      }
      
      // Now route to the first matching Handler
      for (ParserModule h : handlers) {
        if (h.matches(logicalLine)) {
          h.handle(logicalLine);
          return;
        }
      }
	  System.err.println("UNHANDLED → " + logicalLine);
    }
    
    // ------------- Public Fns for Modules ------------
    
    public Object evaluateLiteral(String literal) {
    	// TODO
		return literal;
    }
    
    public boolean isAliasDefined(String alias) {
        for (AliasModule aliasHandler : aliasHandlers) {
            if (aliasHandler.isAlias(alias)) {
            	return true;
            }
        }
        return false;
    }
    
    public boolean isVariableDefined(String var) {
        for (VariableModule variableHandler : variableHandlers) {
            if (variableHandler.isVariable(var)) {
            	return true;
            }
        }
        return false;
    }
    
    // ------------------ Getters ----------------------
    
    // TODO make this a hashset with type as value?
    // Then make all modules implement ReservedWordHandler?
    public Set<String> getAllReservedWords() {
        Set<String> all = new HashSet<>();
        for (ParserModule h : handlers) {
            all.addAll(h.getReservedWords());
        }
        return all;
    }
    
    public Set<String> getAllAliases() {
        Set<String> all = new HashSet<>();
        for (AliasModule aliaser : aliasHandlers) {
            all.addAll(aliaser.getReservedWords());
        }
        return all;
    }
    
    public Set<String> getAllVariables() {
        Set<String> all = new HashSet<>();
        for (VariableModule varHandler : variableHandlers) {
            all.addAll(varHandler.getReservedWords());
        }
        return all;
    }
    
    public ParserModule getHandler(String name) {
        return index.get(name);
    }
    
    public List<AliasModule> getAliasHandlers(String name) {
        return aliasHandlers;
    }
}