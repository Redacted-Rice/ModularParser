package redactedrice.modularparser;


import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface WordReserver {
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
