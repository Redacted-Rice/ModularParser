package redactedrice.modularparser.reserved;

import java.util.Set;

import redactedrice.modularparser.core.Supporter;

public interface ReservedWordSupporter extends Supporter {
    public boolean isReservedWord(String word);
    public Set<String> getReservedWords();
}
