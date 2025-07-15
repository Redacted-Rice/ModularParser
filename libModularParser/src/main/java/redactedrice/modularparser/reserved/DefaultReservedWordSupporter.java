package redactedrice.modularparser.reserved;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redactedrice.modularparser.core.BaseModule;
import redactedrice.modularparser.core.Module;
import redactedrice.modularparser.reserved.WordReserver.ReservedType;

public class DefaultReservedWordSupporter extends BaseModule implements ReservedWordSupporter {
    private final List<WordReserver> reservers = new ArrayList<>();

    public DefaultReservedWordSupporter() {
        super(DefaultReservedWordSupporter.class.getSimpleName());
    }

    @Override
    public boolean handleModule(Module module) {
        if (module instanceof WordReserver) {
            WordReserver asReserver = (WordReserver) module;
            // Check for exclusive reserved-word conflicts
            Set<String> exclusive = asReserver.getReservedWords(ReservedType.EXCLUSIVE);
            for (WordReserver existing : reservers) {
                Map<String, ReservedType> common = new HashMap<>(existing.getAllReservedWords());
                common.keySet().retainAll(exclusive);
                if (!common.isEmpty()) {
                    System.err.println("Module '" + asReserver.getName()
                            + "' exclusively reserves the following keys already reserved by '"
                            + existing.getName() + "': " + common);
                    return false;
                }
            }
            reservers.add(asReserver);
        }
        return true;
    }

    @Override
    public boolean isReservedWord(String word) {
        for (WordReserver reserver : reservers) {
            if (reserver.isReservedWord(word)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<String> getReservedWords() {
        Set<String> all = new HashSet<>();
        // reservers.stream().forEach(reserver -> all.add(reserver.getAllReservedWords()));
        return all;
    }
}
