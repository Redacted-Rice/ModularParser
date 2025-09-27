package redactedrice.modularparser.literal.argumented;


import redactedrice.modularparser.core.Response;

public interface ArgumentParser {	
	public Response<Object> tryParseArgument(Response<Object> parsed, String argument);
}
