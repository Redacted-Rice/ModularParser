package redactedrice.modularparser;

import java.util.Set;

public interface ReservedWord {
	boolean isReservedWord(String word);
    Set<String> getReservedWords();
}
