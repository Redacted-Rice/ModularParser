package redactedrice.modularparser.literal;


import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import redactedrice.modularparser.core.BaseModule;
import redactedrice.modularparser.core.Response;
import redactedrice.reflectionhelpers.utils.ReflectionUtils;

public class DefaultObjectPathParser extends BaseModule
	     implements ChainableLiteralParser {	
	 protected static final Pattern PARAMETERS_PATTERN = Pattern.compile("(\\w+)\\s*\\(([^)]*)\\)");

     protected final String chainingCharacter;
     protected final String argDelimiter;
     
	 protected LiteralSupporter literalSupporter;
	
	 public DefaultObjectPathParser(String name, String chainingCharacter, String argDelimiter) {
	     super(name);
	     this.chainingCharacter = chainingCharacter;
	     this.argDelimiter = argDelimiter;
	 }
	
	 @Override
	 public void setModuleRefs() {
	     literalSupporter = parser.getSupporterOfType(LiteralSupporter.class);
	 }
	
	 protected Response<Object> handleWithArgs(Object chained, String literal) {
		 if (chained == null) {
			 // Not handled by this
	         return Response.notHandled();
		 }
	     String trimmed = literal.trim();
	
	     Matcher m = PARAMETERS_PATTERN.matcher(trimmed);
	     if (!m.find()) {
			 // Not handled by this
	         return Response.notHandled();
	     }
	     
	     String fieldName = m.group(1);
	     String argsString = m.group(2);
	     Object[] argsParsed = new Object[] {};
	     if (!argsString.isBlank()) {
		     String[] args = argsString.split(argDelimiter);
		     
		     // parse args
		     argsParsed = new Object[args.length];
		     for (int i = 0; i < args.length; i++) {
		    	 argsParsed[i] = literalSupporter.evaluateLiteral(args[i]);
		     }
	     }
	     
	     try {
			return Response.is(ReflectionUtils.invoke(chained, fieldName + "()", argsParsed));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException | NoSuchFieldException e) {
			 // Not handled by this but still could be valid for something else
	         return Response.notHandled();
		}
	 }
	
	 @Override
	 public Response<Object> tryParseLiteral(String literal) {
		 if (literal.contains(chainingCharacter)) {
			 // If it has the chaining character, we should handle it
			 Response<Object> ret = literalSupporter.evaluateLiteral(literal);
			 // It should have been handled
			 if (ret.wasNotHandled()) {
				 return Response.error("Failed to parse literal " + literal);
			 }
			 return ret;
		 }
	     return Response.notHandled();
	 }
	
	 @Override
	 public Response<Object> tryEvaluateChainedLiteral(Object chained, String literal) {
		 if (chained == null || literal == null) {
			 return Response.notHandled();
		 }
		 Response<Object> result = handleWithArgs(chained, literal);
		 if (result.wasHandled()) {
			 return result;
		 }
		 try {
			return Response.is(ReflectionUtils.getVariable(chained, literal));
		 } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException | NoSuchFieldException e) {
			 // fall through to empty return
		 }
		 return Response.notHandled();
 }
}
