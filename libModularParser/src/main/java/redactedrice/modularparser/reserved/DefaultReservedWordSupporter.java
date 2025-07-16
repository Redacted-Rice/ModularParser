package redactedrice.modularparser.reserved;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import redactedrice.modularparser.core.BaseModule;
import redactedrice.modularparser.core.Module;

public class DefaultReservedWordSupporter extends BaseModule implements ReservedWordSupporter {
    private final List<WordReserver> reservers = new ArrayList<>();

    public DefaultReservedWordSupporter() {
        super(DefaultReservedWordSupporter.class.getSimpleName());
    }

    @Override
    public void handleModule(Module module) {
        if (module instanceof WordReserver) {
            reservers.add((WordReserver) module);
        }
    }

    @Override
    public boolean checkModulesCompatibility() {
        for (WordReserver beingChecked : reservers) {
            // Check for exclusive reserved-word conflicts
            Set<String> exclusive = beingChecked.getReservedWords(ReservedType.EXCLUSIVE);
            for (WordReserver other : reservers) {
                if (other == beingChecked) {
                    continue;
                }
                Map<String, ReservedType> common = new HashMap<>(other.getAllReservedWords());
                common.keySet().retainAll(exclusive);
                if (!common.isEmpty()) {
                    System.err.println("Module '" + beingChecked.getName()
                            + "' exclusively reserves the following keys already reserved by '"
                            + other.getName() + "': " + common);
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean isReservedWord(String word, Optional<ReservedType> type) {
        for (WordReserver reserver : reservers) {
            if (reserver.isReservedWord(word, type)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<String> getReservedWords(ReservedType type) {
        Set<String> all = new HashSet<>();
        reservers.stream().forEach(reserver -> all.addAll(reserver.getReservedWords(type)));
        return all;
    }

    @Override
    public Map<String, ReservedType> getAllReservedWords() {
        Map<String, ReservedType> all = new HashMap<>();
        reservers.stream().forEach(reserver -> all.putAll(reserver.getAllReservedWords()));
        return all;
    }
}
