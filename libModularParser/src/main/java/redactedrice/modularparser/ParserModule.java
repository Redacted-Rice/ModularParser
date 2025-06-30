package redactedrice.modularparser;


import java.util.Set;

public interface ParserModule {
    String getName();
    void setParser(Parser parser);
    
    boolean matches(String logicalLine);
    void handle(String logicalLine);
    
	boolean isReservedWord(String word);
    Set<String> getReservedWords();
}
