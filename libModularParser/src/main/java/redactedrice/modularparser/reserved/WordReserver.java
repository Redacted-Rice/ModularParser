package redactedrice.modularparser.reserved;


import java.util.Set;

import redactedrice.modularparser.core.Module;

public interface WordReserver extends Module {

    public boolean isReservedWord(String word);

    public Set<String> getReservedWords();
}
