package redactedrice.modularparser.lineformer;


import redactedrice.modularparser.core.Module;

public interface LineModifier extends Module {
    public boolean lineContinuersValid(String line, boolean isLineComplete);

    public boolean lineHasOpenModifier(String line);

    public String modifyLine(String line);

    public static String validStartStopTokens(String line, String startToken, String endToken,
            boolean isComplete) {
        int balance = 0;
        int idx = 0;
        int len = line.length();
        int startLen = startToken.length();
        int stopLen = endToken.length();

        while (idx < len) {
            // Check for startToken at current position
            if (idx + startLen <= len && line.substring(idx, idx + startLen).equals(startToken)) {
                balance++;
                idx += startLen;
                continue;
            }

            // Check for stopToken at current position
            if (idx + stopLen <= len && line.substring(idx, idx + stopLen).equals(endToken)) {
                // Out-of-order stop if no matching start
                if (balance == 0) {
                    return "Start tokens " + startToken + " and end tokens " + endToken
                            + " are out of order";
                }
                balance--;
                idx += stopLen;
                continue;
            }

            // Advance by one character otherwise
            idx++;
        }

        // If incomplete pairs are disallowed, require all starts to be closed
        if (!isComplete || (balance == 0)) {
            return "";
        } else {
            return "Mismatched number of Start tokens " + startToken + " and end tokens "
                    + endToken;
        }
    }
}
