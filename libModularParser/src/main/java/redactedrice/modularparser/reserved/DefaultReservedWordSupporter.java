package redactedrice.modularparser.reserved;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import redactedrice.modularparser.core.BaseSupporter;
import redactedrice.modularparser.core.LogSupporter.LogLevel;

public class DefaultReservedWordSupporter extends BaseSupporter<WordReserver>
        implements ReservedWordSupporter {
    public DefaultReservedWordSupporter() {
        super(DefaultReservedWordSupporter.class.getSimpleName(), WordReserver.class);
    }

    @Override
    public boolean checkModulesCompatibility() {
        for (WordReserver beingChecked : submodules) {
            // Check for exclusive reserved-word conflicts
            Set<String> exclusive = beingChecked.getReservedWords(ReservedType.EXCLUSIVE);
            for (WordReserver other : submodules) {
                if (other == beingChecked) {
                    continue;
                }
                Map<String, ReservedType> common = new HashMap<>(other.getAllReservedWords());
                common.keySet().retainAll(exclusive);
                if (!common.isEmpty()) {
                    log(LogLevel.ERROR,
                            "Module '%s' exclusively reserves the following keys already reserved by '%s': %s",
                            beingChecked.getName(), other.getName(), common);
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean isReservedWord(String word, Optional<ReservedType> type) {
        if (word == null || word.isBlank()) {
            return false;
        }

        for (WordReserver reserver : submodules) {
            if (reserver.isReservedWord(word, type)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<String> getReservedWords(ReservedType type) {
        Set<String> all = new HashSet<>();
        submodules.stream().forEach(reserver -> all.addAll(reserver.getReservedWords(type)));
        return all;
    }

    @Override
    public Map<String, ReservedType> getAllReservedWords() {
        Map<String, ReservedType> all = new HashMap<>();
        submodules.stream().forEach(reserver -> all.putAll(reserver.getAllReservedWords()));
        return all;
    }
}
