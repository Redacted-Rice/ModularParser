package redactedrice.modularparser.basic;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import redactedrice.modularparser.LineHandler;
import redactedrice.modularparser.WordReserver;

public abstract class LineStartMatchModule extends BaseModule implements LineHandler, WordReserver {
    protected final Map<String, ReservedType> reservedWords;

    protected LineStartMatchModule(String name, String... exclusiveWords) {
        super(name);

        this.reservedWords = new HashMap<>();
        for (String word : exclusiveWords) {
            this.reservedWords.put(word, ReservedType.EXCLUSIVE);
        }
    }

    @Override
    public boolean matches(String logicalLine) {
        if (logicalLine == null || logicalLine.isBlank()) {
            return false;
        }

        String[] words = logicalLine.trim().split("\\s+", 2);
        return !words[0].isEmpty() && reservedWords.containsKey(words[0]);
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