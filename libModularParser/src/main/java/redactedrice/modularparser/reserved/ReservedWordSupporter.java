package redactedrice.modularparser.reserved;


import java.util.Set;

import redactedrice.modularparser.core.Supporter;

public interface ReservedWordSupporter extends Supporter {

    public default boolean isReservedWord(String word) {
        return null != getReservedWordOwner(word);
    }

    public String getReservedWordOwner(String word);

    public Set<String> getReservedWords();
}
