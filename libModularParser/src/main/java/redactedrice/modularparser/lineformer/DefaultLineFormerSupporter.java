package redactedrice.modularparser.lineformer;


import java.io.BufferedReader;
import java.io.IOException;

import redactedrice.modularparser.core.BaseSupporter;
import redactedrice.modularparser.core.LineFormerSupporter;
import redactedrice.modularparser.core.LogSupporter.LogLevel;
import redactedrice.modularparser.core.Response;

public class DefaultLineFormerSupporter extends BaseSupporter<LineModifier>
        implements LineFormerSupporter {
    protected BufferedReader reader;
    protected int lineNumberStart = 0;
    protected int lineNumberEnd = 0;

    public DefaultLineFormerSupporter() {
        super(DefaultLineFormerSupporter.class.getSimpleName(), LineModifier.class);
    }

    public void setReader(BufferedReader reader) {
        this.reader = reader;
        lineNumberStart = 0;
        lineNumberEnd = 0;
    }

    public void resetReader() {
        if (reader != null) {
            try {
                reader.reset();
                lineNumberStart = 0;
                lineNumberEnd = 0;
                parser.resetStatus();
            } catch (IOException e) {
                log(LogLevel.ERROR, "Failed to reset reader");
            }
        }
    }

    @Override
    public String getNextLogicalLine() {
        String raw = getUntilNextLine();
        if (raw == null) {
            return null;
        }
        String logical = raw;

        // For each modifier, get lines until it closes
        Response<String> gathered;
        do {
            gathered = Response.notHandled();
            for (int modifierIdx = 0; !gathered.wasValueReturned() &&
                    modifierIdx < submodules.size(); modifierIdx++) {
                LineModifier modifier = submodules.get(modifierIdx);
                gathered = gatherLine(modifier, logical);
                if (gathered.wasError()) {
                    return null;
                } else if (gathered.wasValueReturned()) {
                    logical = gathered.value();
                }
                // Modify the line and restart if needed
                logical = modifier.modifyLine(logical);
            }
        } while (gathered.wasHandled());
        // if the line is blank, move to the next one
        if (logical.isBlank()) {
            return getNextLogicalLine();
        }
        return logical;
    }

    protected Response<String> gatherLine(LineModifier modifier, String firstLine) {
        boolean isValid = true;
        boolean modified = false;
        String logical = firstLine;
        while ((isValid = modifier.lineContinuersValid(logical, false)) &&
                modifier.lineHasOpenModifier(logical)) {
            modified = true;
            String raw = getNextLine();
            if (raw == null) {
                log(LogLevel.ABORT,
                        "Failed to read next line while still looking for closed modifier for %s: %s",
                        modifier.getName(), logical);
                return Response
                        .error("Failed to read next line while still looking for closed modifier");
            }
            // We need it each loop so no real benefit of using StringBuilder
            logical += raw; // NOSONAR
        }

        if (!isValid) {
            log(LogLevel.ERROR,
                    "Modifier %s expected to handle Logical line but determined is not valid: %s",
                    modifier.getName(), logical);
            return Response.error("Modifier " + modifier.getName()
                    + " expected to handle Logical line but determined is not valid");
        }
        if (modified) {
            return Response.is(logical);
        }
        return Response.notHandled();
    }

    public String getUntilNextLine() {
        lineNumberStart = lineNumberEnd + 1;
        String line = getNextLine();
        // Keep grabbing until we get a line or run out
        while (line != null && line.isBlank()) {
            line = getNextLine();
        }
        if (line == null) {
            return null;
        }
        return line;
    }

    public String getNextLine() {
        if (reader == null) {
            return null;
        }
        try {
            lineNumberEnd++;
            return reader.readLine();
        } catch (IOException e) {
            // TODO: Separate out expected case from failures?
            lineNumberEnd--;
            return null;
        }
    }

    @Override
    public LineRange getCurrentLineRange() {
        return new LineRange(lineNumberStart, lineNumberEnd);
    }
}
