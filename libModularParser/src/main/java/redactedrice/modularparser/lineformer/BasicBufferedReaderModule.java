package redactedrice.modularparser.lineformer;


import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import redactedrice.modularparser.BaseModule;
import redactedrice.modularparser.Module;

public class BasicBufferedReaderModule extends BaseModule implements LineFormerSupporter {
    protected final List<LineModifier> modifiers = new ArrayList<>();

    protected BufferedReader reader;

    public BasicBufferedReaderModule() {
        super("VariableSupportModule");
    }

    public void setReader(BufferedReader reader) {
        this.reader = reader;
    }

    @Override
    public void addLineModifier(LineModifier modifier) {
        modifiers.add(modifier);
        if (modifier instanceof Module) {
            parser.addModule((Module) modifier);
        }
    }

    @Override
    public String getNextLogicalLine() {
        String raw = getNextLine();
        if (raw == null) {
            return null;
        }
        String logical = raw;

        // For each modifier, get lines until it closes
        for (int modifierIdx = 0; modifierIdx < modifiers.size(); modifierIdx++) {
            LineModifier modifier = modifiers.get(modifierIdx);
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
            return reader.readLine();
        } catch (IOException e) {
            return null;
        }
    }
}
