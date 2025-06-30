package redactedrice.modularparser.basic;


import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import redactedrice.modularparser.VariableModule;

public class BasicVariableModule extends LineStartMatchModule implements VariableModule {
	private final static Pattern varDef = Pattern.compile("^\\s*variable\\s+(\\w+)\\s*=\\s*(.+)$");
	
	private final Map<String, Object> variables = new LinkedHashMap<>();
	
	protected BasicVariableModule() {
		super("BasicVariableHandler", "variable");
	}
	
	/** Retrieve all defined variables (immutable view). */
	public Map<String,Object> getVariables() {
		return Collections.unmodifiableMap(variables);
	}
	
	@Override
	public void handle(String line) {
		Matcher m = varDef.matcher(line);
		if (!m.find()) {
			return;  // shouldn't happen if matches() used right
		}
		
		// Check for previous def
		// TODO: Interface for "reserved" words - commands, alias, and vars?
		// Seems like it would make sense and make checking things easier
		
		String varName = m.group(1);
		String literal = m.group(2).trim();
		
		Object obj = parser.evaluateLiteral(literal);
		if (obj != null) {
			variables.put(varName, obj);
			System.out.println("Variable: Added variable " + varName + " with value: " + literal);
		} else {
			throw new IllegalArgumentException(
			  "VariableHandler: For variable \"" + varName + "\" + cannot parse value: " + literal
			);
		}
	}

	@Override
	public boolean isReservedWord(String word) {
		return super.isReservedWord(word) || isVariable(word);
	}

	@Override
	public Set<String> getReservedWords() {
		Set<String> all = super.getReservedWords();
		all.addAll(variables.keySet());
		return all;
	}

	@Override
	public boolean isVariable(String alias) {
		return variables.containsKey(alias);
	}
}

