package redactedrice.modularparser;


import java.util.Set;

public interface WordReserver {
    boolean isReservedWord(String word);

    Set<String> getReservedWords();
}
