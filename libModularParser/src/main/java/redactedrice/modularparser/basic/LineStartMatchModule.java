package redactedrice.modularparser.basic;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import redactedrice.modularparser.LineHandler;
import redactedrice.modularparser.ReservedWord;

public abstract class LineStartMatchModule extends BaseModule implements LineHandler, ReservedWord {
    protected final Set<String> reservedWords;

    protected LineStartMatchModule(String name, String... reservedWords) {
    	super(name);
        
        this.reservedWords = new HashSet<>();
        for (String word : reservedWords) {
        	this.reservedWords.add(word);
        }
    }

	@Override
	public boolean matches(String logicalLine) {
        if (logicalLine == null || logicalLine.isBlank()) {
        	return false;
        }

        String[] words = logicalLine.trim().split("\\s+", 2);
        return !words[0].isEmpty() && reservedWords.contains(words[0]);
	}

	@Override
	public boolean isReservedWord(String word) {
		return reservedWords.contains(word);
	}

	@Override
	public Set<String> getReservedWords() {
		return Collections.unmodifiableSet(reservedWords);
	}
	
}