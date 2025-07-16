package redactedrice.modularparser.reserved;


import java.util.Map;
import java.util.Optional;
import java.util.Set;

import redactedrice.modularparser.core.Supporter;

public interface ReservedWordSupporter extends Supporter {
    public enum ReservedType {
        EXCLUSIVE, SHAREABLE
    }

    public default boolean isReservedWord(String word) {
        return isReservedWord(word, Optional.empty());
    }

    public boolean isReservedWord(String word, Optional<ReservedType> type);

    public Set<String> getReservedWords(ReservedType type);

    public Map<String, ReservedType> getAllReservedWords();

}
