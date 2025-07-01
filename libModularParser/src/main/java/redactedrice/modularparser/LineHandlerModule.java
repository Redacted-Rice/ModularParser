package redactedrice.modularparser;


import java.util.Set;

public interface LineHandlerModule extends Module {    
    boolean matches(String logicalLine);
    void handle(String logicalLine);
    
	boolean isReservedWord(String word);
    Set<String> getReservedWords();
}
