package redactedrice.modularparser.lineformer;


import java.util.Optional;
import java.util.regex.Pattern;

import redactedrice.modularparser.core.BaseModule;
import redactedrice.modularparser.core.LogSupporter.LogLevel;
import redactedrice.modularparser.core.Response;

// This is for grouping like typically done with ()
// This is not for scoping like is done with {} or indentations
public class DefaultGroupingLineModifier extends BaseModule implements LineModifier, Grouper {
    protected static final Pattern NEWLINE_PATTERN = Pattern.compile("\\s*\\R\\s*");

    protected final String startToken;
    protected final String endToken;
    protected final boolean removeTokens;
    protected final Pattern startTokenRegex;
    protected final Pattern endTokenRegex;

    public DefaultGroupingLineModifier(String name, String startToken, String endToken,
            boolean removeTokens) {
        super(name);
        this.startToken = startToken;
        this.endToken = endToken;
        this.removeTokens = removeTokens;
        startTokenRegex = Pattern.compile("\\s*" + Pattern.quote(startToken) + "\\s*");
        endTokenRegex = Pattern.compile("\\s*" + Pattern.quote(endToken) + "\\s*");
    }

    @Override
    public boolean lineContinuersValid(String line, boolean isComplete) {
        Optional<String> error = validStartStopTokens(line, isComplete);
        if (error.isPresent()) {
            log(LogLevel.ERROR, error.get());
            return false;
        }
        return true;
    }

    @Override
    public boolean lineHasOpenModifier(String line) {
        return countOccurrences(line, startToken) > countOccurrences(line, endToken);
    }

    @Override
    public String modifyLine(String line) {
        // We only need to test for one because it will have both
        if (line.contains(startToken)) {
            line = NEWLINE_PATTERN.matcher(line).replaceAll(" ");
            if (removeTokens) {
                line = endTokenRegex.matcher(startTokenRegex.matcher(line).replaceAll(" "))
                        .replaceAll(" ");
            } else {
                line = endTokenRegex
                        .matcher(startTokenRegex.matcher(line).replaceAll(" " + startToken))
                        .replaceAll(endToken + " ");
            }
        }
        return line;
    }

    @Override
    public Response<String[]> tryGetNextGroup(String line, boolean stripTokens) {
        Response<String[]> response = tryGetGroupHelper(line, true, true);
        // If we got a completed response return it
        if (response.wasValueReturned() && response.getValue()[2] != null) {
            return response;
        }
        // Otherwise return not handled
        return Response.notHandled();
    }

    protected Response<String[]> tryGetGroupHelper(String line, boolean isComplete,
            boolean stripTokens) {
        int balance = 0;
        int startIdx = -1;
        int idx = 0;
        int len = line.length();
        int startLen = startToken.length();
        int stopLen = endToken.length();

        while (idx < len) {
            if (idx + startLen <= len && line.substring(idx, idx + startLen).equals(startToken)) {
                if (balance == 0) {
                    startIdx = idx; // mark the beginning of the first complete match
                }
                balance++;
                idx += startLen;
            } else if (idx + stopLen <= len &&
                    line.substring(idx, idx + stopLen).equals(endToken)) {
                balance--;
                idx += stopLen;

                if (balance == 0) {
                    return makeCompleteResponse(line, startIdx, idx, stripTokens);
                } else if (balance < 0) {
                    return Response.error("Start tokens " + startToken + " and end tokens "
                            + endToken + " are out of order");
                }
            } else {
                idx++;
            }
        }
        return makeNotCompleteResponse(line, isComplete, balance, startIdx);
    }

    protected Response<String[]> makeCompleteResponse(String line, int startIdx, int endIdx,
            boolean stripTokens) {
        String preMatch = line.substring(0, startIdx);
        String matched;
        if (!stripTokens) {
            matched = line.substring(startIdx, endIdx);
        } else {
            matched = line.substring(startToken.length(), endIdx - endToken.length());
        }
        String postMatch = line.substring(endIdx);
        return Response.is(new String[] {preMatch, matched, postMatch});
    }

    protected Response<String[]> makeNotCompleteResponse(String line, boolean isComplete,
            int balance, int startIdx) {
        if (!isComplete || balance == 0) {
            if (startIdx < 0) {
                return Response.is(new String[] {line, null, null});
            }
            String preMatch = line.substring(0, startIdx);
            String match = line.substring(startIdx);
            return Response.is(new String[] {preMatch, match, null});
        } else {
            return Response.error("Mismatched number of Start tokens " + startToken
                    + " and end tokens " + endToken);
        }
    }

    protected int countOccurrences(String str, String sub) {
        int count = 0;
        int index = 0;

        while ((index = str.indexOf(sub, index)) != -1) {
            count++;
            index += sub.length(); // move past the current match
        }

        return count;
    }

    protected Optional<String> validStartStopTokens(String line, boolean isComplete) {
        Response<String[]> response = tryGetGroupHelper(line, isComplete, false);

        if (response.wasValueReturned()) {
            if (response.getValue()[1] == null) {
                // no tokens. Its valid
                return Optional.empty();
            }

            // Had some tokens but they weren't complete
            if (response.getValue()[2] == null) {
                return Optional.empty();
            }

            // Have a complete set of tokens
            if (response.getValue()[2].isEmpty()) {
                // No more trailing, we are good
                return Optional.empty();
            } else {
                // otherwise, check for another pair in the trailing
                return validStartStopTokens(response.getValue()[2], isComplete);
            }
        } else if (response.wasNotHandled()) {
            // Shouldn't hit this
            return Optional.of("Internal logic error");
        }
        // Malformed tokens
        return Optional.of(response.getError());
    }

    @Override
    public boolean isEmptyGroup(String string) {
        String trimmed = string.trim();
        if (!trimmed.startsWith(startToken) || !trimmed.endsWith(endToken)) {
            return false;
        }
        return trimmed.substring(startToken.length(), trimmed.length() - endToken.length())
                .isBlank();
    }

    @Override
    public boolean hasOpenGroup(String line) {
        return lineHasOpenModifier(line);
    }
}
