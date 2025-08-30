package redactedrice.modularparser.literal;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import redactedrice.modularparser.core.BaseModule;
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
	
	 protected Optional<Object> handleWithArgs(Object chained, String literal) {
	     if (literal == null) {
	         return Optional.empty();
	     }
	     String trimmed = literal.trim();
	
	     Matcher m = PARAMETERS_PATTERN.matcher(trimmed);
	     if (!m.find()) {
	         return Optional.empty();
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
			Optional<Object> result = Optional.ofNullable(ReflectionUtils.invoke(chained, fieldName + "()", argsParsed));
			if (result.isEmpty()) {
				return Optional.ofNullable(new SuccessNoReturn(fieldName + "()"));
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException | NoSuchFieldException e) {
	         return Optional.empty();
		}
	 }
	
	 @Override
	 public Optional<Object> tryParseLiteral(String literal) {
		 if (literal.contains(chainingCharacter)) {
			 // Cannot evaluate by itself - must be on an object
			 Object result = literalSupporter.evaluateLiteral(literal);
			 if (result instanceof SuccessNoReturn) {
				 return Optional.empty();
			 }
			 return Optional.ofNullable(result);
		 }
	     return Optional.empty();
	 }
	
	 @Override
	 public Optional<Object> tryEvaluateChainedLiteral(Object chained, String literal) {
		 if (chained == null || literal == null) {
			 return Optional.empty();
		 }
		 Optional<Object> result = handleWithArgs(chained, literal);
		 if (result.isPresent()) {
			 return result;
		 }
		 try {
			result = Optional.ofNullable(ReflectionUtils.getVariable(chained, literal));
			if (result.isEmpty()) {
				return Optional.ofNullable(new SuccessNoReturn(literal));
			}
		 } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException | NoSuchFieldException e) {
			 // fall through to empty return
		 }
		 return Optional.empty();
 }
}
