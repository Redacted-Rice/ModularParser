package redactedrice.modularparser.reserved;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import redactedrice.modularparser.core.BaseModule;
import redactedrice.modularparser.reserved.ReservedWordSupporter.ReservedType;

public class BaseWordReserver extends BaseModule implements WordReserver {
    protected final Map<String, ReservedType> reservedWords = new HashMap<>();

    protected BaseWordReserver(String name) {
        super(name);
    }

    @Override
    public boolean isReservedWord(String word, Optional<ReservedType> type) {
        if (type.isEmpty()) {
            return reservedWords.containsKey(word);
        }
        return reservedWords.get(word) == type.get();
    }

    @Override
    public Map<String, ReservedType> getAllReservedWords() {
        return Collections.unmodifiableMap(reservedWords);
    }

    @Override
    public Set<String> getReservedWords(ReservedType type) {
        return Set.copyOf(reservedWords.entrySet().stream()
                .filter(entry -> entry.getValue() == type).map(Map.Entry::getKey).toList());
    }
}
