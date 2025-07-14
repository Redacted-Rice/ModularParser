package redactedrice.modularparser.reserved;


import java.util.Map;
import java.util.Optional;
import java.util.Set;

import redactedrice.modularparser.core.Module;

public interface WordReserver extends Module {
    public enum ReservedType {
        EXCLUSIVE, SHAREABLE
    }

    default boolean isReservedWord(String word) {
        return isReservedWord(word, Optional.empty());
    }
    
    boolean isReservedWord(String word, Optional<ReservedType> type);

    Map<String, ReservedType> getAllReservedWords();

    Set<String> getReservedWords(ReservedType type);
}
