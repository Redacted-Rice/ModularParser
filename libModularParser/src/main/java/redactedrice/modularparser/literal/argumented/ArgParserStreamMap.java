package redactedrice.modularparser.literal.argumented;


import java.util.stream.Stream;


public class ArgParserStreamMap extends ArgParserValueTypedMapBase {
    public ArgParserStreamMap(boolean allowNull) {
        super(allowNull);
    }

	@Override
	protected Class<?> expectedType() {
		return Stream.class;
	}
}
