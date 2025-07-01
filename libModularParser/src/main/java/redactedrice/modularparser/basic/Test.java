package redactedrice.modularparser.basic;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import redactedrice.modularparser.Parser;

// Simple test for development to check the behavior is as expected
public class Test {
    public static void main(String[] args) throws IOException {
		Parser parser = new Parser();
		
		parser.addLineContinue("\\");
		parser.addLineContinue("->");
		parser.addSingleLineComment("//");
		parser.addSingleLineComment("#");
		parser.addMultiLineComment("/*", "*/");
	
		parser.addModule(new LambdaModule(
		    "definitions",
		    line -> System.out.println("DEF → " + line),
		    "def"
		));

		parser.addModule(new BasicNumberParser());
		parser.addModule(new BasicAliasModule());
		parser.addModule(new BasicVariableModule());
		
		parser.addModule(new LambdaModule(
			    "TestPrintHandler",
			    line -> System.out.println("Print: " + line.substring(8))
			));
	
	    // Test script as a multiline string
	    String script = """
	      # This is a comment (
	      /* This is (
	         a block comment */
	      variable foo = 42
	      let bar = true ->
	         and something 
	      def myFunc(x) \\
	        println x 
	      // Some comment  (
	      alias greet = println "Hello" 
	      alias greet = println "Hello 2" 
	      alias def = println "Hello 3" 
	      def myFunc(x) \\
	        println x 
	      // a comment end)
	      greet
	      (greet) // TODO handle this case
	      """;
	
	    // Run parser
	    parser.parse(new BufferedReader(new StringReader(script)));
	    

		// Unhappy test cases
//		parser.addModule(new SimpleHandler(
//			    "definitions",
//			    line -> line.trim().startsWith("def "),
//			    line -> System.out.println("DEF → " + line),
//			    "defs"
//			));
//		
//		parser.addModule(new SimpleHandler(
//			    "definitions2",
//			    line -> line.trim().startsWith("def "),
//			    line -> System.out.println("DEF → " + line),
//			    "def"
//			));
	  }
}
