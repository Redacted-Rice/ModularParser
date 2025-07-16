package redactedrice.modularparser.reserved;


import java.util.Map;
import java.util.Optional;
import java.util.Set;

import redactedrice.modularparser.core.Module;
import redactedrice.modularparser.reserved.ReservedWordSupporter.ReservedType;

public interface WordReserver extends Module {
    default boolean isReservedWord(String word) {
        return isReservedWord(word, Optional.empty());
    }

    boolean isReservedWord(String word, Optional<ReservedType> type);

    Map<String, ReservedType> getAllReservedWords();

    Set<String> getReservedWords(ReservedType type);
}
