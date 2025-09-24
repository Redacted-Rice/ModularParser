package redactedrice.modularparser.literal.argumented;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArgumentsDefinition {
    protected final List<String> requiredArgs;
    protected final List<String> optionalArgs;
    protected final List<Object> optionalDefaults;
    protected final Map<String, ArgumentParser> argParsers;

    public ArgumentsDefinition(String[] requiredArgs, String[] optionalArgs,
            Object[] optionalDefaults, ArgumentParser[] argParsers) {

        if (requiredArgs == null) {
            this.requiredArgs = new ArrayList<>();
        } else {
            this.requiredArgs = List.of(requiredArgs);
        }

        if (optionalArgs == null) {
            this.optionalArgs = new ArrayList<>();
        } else {
            this.optionalArgs = List.of(optionalArgs);
        }

        if (optionalDefaults == null) {
            this.optionalDefaults = new ArrayList<>();
        } else {
            this.optionalDefaults = List.of(optionalDefaults);
        }

        // Ensure size of optionals and defaults match
        if (this.optionalDefaults.size() != this.optionalArgs.size()) {
            throw new IllegalArgumentException("optionalDefaults (" + this.optionalDefaults.size()
                    + ") must be the same length as optionalArgs (" + this.optionalArgs.size()
                    + ")");
        }
        this.argParsers = new HashMap<>();
        setArgParsers(argParsers);
    }

    protected void setArgParsers(ArgumentParser[] argParsers) {
        if (argParsers == null || argParsers.length < 1) {
            for (String required : requiredArgs) {
                this.argParsers.put(required, new TypeUnenforced());
            }
            for (String optional : optionalArgs) {
                this.argParsers.put(optional, new TypeUnenforced());
            }
        } else if (argParsers.length == getNumArgs()) {
            int requiredIdx = 0;
            int optionalIdx = 0;
            for (requiredIdx = 0; requiredIdx < getNumRequiredArgs(); requiredIdx++) {
                this.argParsers.put(requiredArgs.get(requiredIdx), argParsers[requiredIdx]);
            }
            for (optionalIdx = 0; optionalIdx < getNumOptionalArgs(); optionalIdx++) {
                this.argParsers.put(optionalArgs.get(optionalIdx),
                        argParsers[requiredIdx + optionalIdx]);
            }
        } else {
            throw new IllegalArgumentException(
                    "ArgParsers must be either null/empty or of length requiredArgs + optionalArgs");
        }
    }

    public int getNumRequiredArgs() {
        return requiredArgs.size();
    }

    public int getNumOptionalArgs() {
        return optionalArgs.size();
    }

    public int getNumArgs() {
        return requiredArgs.size() + optionalArgs.size();
    }
}
