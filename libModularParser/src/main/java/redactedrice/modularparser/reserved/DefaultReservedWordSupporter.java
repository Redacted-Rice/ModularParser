package redactedrice.modularparser.reserved;


import java.util.HashSet;
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
            Set<String> thisWords = beingChecked.getReservedWords();
            for (WordReserver other : submodules) {
                if (other == beingChecked) {
                    continue;
                }
                Set<String> common = new HashSet<>(other.getReservedWords());
                common.retainAll(thisWords);
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
    public String getReservedWordOwner(String word) {
        if (word == null || word.isBlank()) {
            return null;
        }

        for (WordReserver reserver : submodules) {
            if (reserver.isReservedWord(word)) {
                return reserver.getName();
            }
        }
        return null;
    }

    @Override
    public Set<String> getReservedWords() {
        Set<String> all = new HashSet<>();
        submodules.stream().forEach(reserver -> all.addAll(reserver.getReservedWords()));
        return all;
    }
}
