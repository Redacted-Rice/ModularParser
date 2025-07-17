package redactedrice.modularparser.lineformer;


import java.io.BufferedReader;
import java.io.IOException;

import redactedrice.modularparser.core.BaseSupporter;
import redactedrice.modularparser.core.LineFormerSupporter;

public class DefaultLineFormerSupporter extends BaseSupporter<LineModifier>
        implements LineFormerSupporter {
    protected BufferedReader reader;
    protected int lineNumberStart = 0;
    protected int lineNumberEnd = 0;

    public DefaultLineFormerSupporter() {
        super("LineFormerSupportModule", LineModifier.class);
    }

    public void setReader(BufferedReader reader) {
        this.reader = reader;
        lineNumberStart = 0;
        lineNumberEnd = 0;
    }

    public void resetReader() {
        try {
            reader.reset();
            lineNumberStart = 0;
            lineNumberEnd = 0;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public String getNextLogicalLine() {
        lineNumberStart = lineNumberEnd + 1;
        String raw = getNextLine();
        if (raw == null) {
            return null;
        }
        String logical = raw;

        // For each modifier, get lines until it closes
        for (int modifierIdx = 0; modifierIdx < submodules.size(); modifierIdx++) {
            LineModifier modifier = submodules.get(modifierIdx);
            boolean addedLine = false;
            while (modifier.hasOpenModifier(logical)) {
                raw = getNextLine();
                addedLine = true;
                if (raw == null) {
                    return null;
                }
                logical += raw;
            }
            // Modify the line and restart if needed
            logical = modifier.modifyLine(logical);
            // if the line is blank, move to the next one
            if (logical.isBlank()) {
                return getNextLogicalLine();
            }
            // If we added to the line, we need to restart
            if (addedLine) {
                modifierIdx = -1;
                continue;
            }
        }
        return logical;

    }

    public String getNextLine() {
        try {
            lineNumberEnd++;
            return reader.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public LineRange getCurrentLineRange() {
        return new LineRange(lineNumberStart, lineNumberEnd);
    }
}
