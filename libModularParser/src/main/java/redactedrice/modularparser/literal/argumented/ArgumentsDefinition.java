package redactedrice.modularparser.literal.argumented;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArgumentsDefinition {
    protected final List<String> requiredArgs;
    protected final List<String> optionalArgs;
    protected final List<Object> optionalDefaults;
    protected final Map<String, ArgumentParser> argParsers;

    public ArgumentsDefinition() {
    	this(null, null, null, null);
    }
    
    public ArgumentsDefinition(String[] requiredArgs, String[] optionalArgs,
            Object[] optionalDefaults, ArgumentParser[] argParsers) {

        if (requiredArgs == null) {
            this.requiredArgs = new ArrayList<>();
        } else {
            this.requiredArgs = Arrays.asList(requiredArgs);
        }

        if (optionalArgs == null) {
            this.optionalArgs = new ArrayList<>();
        } else {
            this.optionalArgs = Arrays.asList(optionalArgs);
        }

        if (optionalDefaults == null) {
            this.optionalDefaults = new ArrayList<>();
        } else {
            this.optionalDefaults = Arrays.asList(optionalDefaults);
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
        if (argParsers == null) {
            for (String required : requiredArgs) {
                this.argParsers.put(required, new ArgParserAny(true));
            }
            for (String optional : optionalArgs) {
                this.argParsers.put(optional, new ArgParserAny(true));
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

    public String getRequiredArg(int index) {
        if (index < 0 || index >= getNumRequiredArgs()) {
            return null;
        }
        return requiredArgs.get(index);
    }

    public String getOptionalArg(int index) {
        if (index < 0 || index >= getNumOptionalArgs()) {
            return null;
        }
        return optionalArgs.get(index);
    }

    public String getArg(int index) {
        String arg = getRequiredArg(index);
        if (arg == null) {
            return getOptionalArg(index - getNumRequiredArgs());
        }
        return arg;
    }

    public Object getOptionalDefault(int index) {
        if (index < 0 || index >= getNumOptionalArgs()) {
            return null;
        }
        return optionalDefaults.get(index);
    }

    public ArgumentParser getArgParser(String arg) {
        return argParsers.get(arg);
    }
}
