package redactedrice.modularparser.basic;


import java.util.Map;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.regex.*;

import redactedrice.modularparser.AliasModule;

public class BasicAliasModule extends LineStartMatchModule implements AliasModule {
	private final static Pattern aliasDef = Pattern.compile("^\\s*alias\\s+(\\w+)\\s*=\\s*(.+)$");
	
	private final Map<String,String> aliases = new LinkedHashMap<>();
	
	public BasicAliasModule() { 
		super("BasicAliasHandler", "alias");
	}
	
	@Override
	public void handle(String line) {
	    Matcher m = aliasDef.matcher(line);
	    if (!m.find()) {
	    	return;
	    }
	
	    String key = m.group(1);
	    String val = m.group(2).trim();
	    // strip quotes
	    if ((val.startsWith("\"") && val.endsWith("\"")) ||
	        (val.startsWith("'") && val.endsWith("'"))) {
	        val = val.substring(1, val.length() - 1);
	    }

	    // Check for collisions with reserved words
	    if (parser.getAllReservedWords().contains(key)) {
	        System.err.println("Warning: alias '" + key +
	                           "' conflicts reserved word and will be ignored!");
	        return;
	    }
	    
	    // Check for collisions with already defined alias
	    if (parser.getAllAliases().contains(key)) {
	        System.err.println("Warning: alias '" + key +
	                           "' conflicts already defined alias and will be ignored!");
	        return;
	    }
		System.out.println("Alias: Added alias " + key + " with value: " + val);
	    aliases.put(key, val);
	}

	@Override
	public boolean isReservedWord(String word) {
		return super.isReservedWord(word) || isAlias(word);
	}

	@Override
	public Set<String> getReservedWords() {
		Set<String> all = super.getReservedWords();
		all.addAll(aliases.keySet());
		return all;
	}
	
	@Override
	public String replaceAliases(String line) {
	    if (matches(line)) return line;
	    String out = line;
	    for (Map.Entry<String,String> e : aliases.entrySet()) {
	        out = out.replaceAll("\\b" + Pattern.quote(e.getKey()) + "\\b",
	                             Matcher.quoteReplacement(e.getValue()));
	    }
	    return out;
	}

	@Override
	public boolean isAlias(String alias) {
		return aliases.containsKey(alias);
	}
}