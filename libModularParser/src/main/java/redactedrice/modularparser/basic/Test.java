package redactedrice.modularparser.basic;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import redactedrice.modularparser.Parser;
import redactedrice.modularparser.WordReserver;

// Simple test for development to check the behavior is as expected
public class Test {
    static private class ShareableReservedTest extends BaseModule implements WordReserver {
        Set<String> sharedWords = new HashSet<>();

        protected ShareableReservedTest(String name, String... shareables) {
            super(name);
            for (String shareable : shareables) {
                sharedWords.add(shareable);
            }
        }

        @Override
        public boolean isReservedWord(String word, Optional<ReservedType> type) {
            // Not needed
            return false;
        }

        @Override
        public Map<String, ReservedType> getAllReservedWords() {
            Map<String, ReservedType> all = new HashMap<>();
            for (String word : sharedWords) {
                all.put(word, ReservedType.SHAREABLE);
            }
            return all;
        }

        @Override
        public Set<String> getReservedWords(ReservedType type) {
            if (type == ReservedType.SHAREABLE) {
                return sharedWords;
            }
            return Collections.emptySet();
        }
    };

    public static void main(String[] args) throws IOException {
        Parser parser = new Parser();

        parser.addLineContinue("\\");
        parser.addLineContinue("->");
        parser.addSingleLineComment("//");
        parser.addSingleLineComment("#");
        parser.addMultiLineComment("/*", "*/");

        parser.addModule(new BasicLambdaModule("definitions",
                line -> System.out.println("DEF → " + line), "def"));

        parser.addModule(new BasicNumberParser());
        parser.addModule(new BasicCharParser());
        parser.addModule(new BasicBoolParser());
        parser.addModule(new BasicAliasModule());
        parser.addModule(new BasicVariableModule());

        parser.addModule(new BasicLambdaModule("TestPrintHandler",
                line -> System.out.println("Print: " + line.substring(8)), "println"));

        // Test script as a multiline string
        String script = """
                # This is a comment (
                /* This is (
                   a block comment */
                variable num = 42
                variable num2 = 42.3
                variable num3 = 42L
                variable num4 = 42.3d
                variable num5 = 42i
                variable num6 = 42.3e3
                variable num7 = \\
                  42e3
                variable num8 = 42e3L
                variable num9 = num
                variable str = "This is a string test"
                variable ch = '\t'
                variable bool1 = TRUE
                variable bool2 = f
                variable bar = "true ->
                   and something"
                def myFunc(x) \\
                  println x
                // Some comment  (
                alias greet = println "Hello"
                alias greet = println "Hello 2"
                alias def = println "Hello 3"
                // a comment end)
                greet
                (greet)
                println str
                println "Trailing comment" // Comment at the end
                variable annoying = /*just to be annoying*/ 5
                variable strange = "interstingly /* you
                  can insert a comment like this
                  across multiple lines and */ this works"
                variable anotherCase = \\
                    /* test a multi line comment on a continuation
                    line as well */ "works too"
                variable yetAnother = \\
                    "works" // test a single line comment on a continuation
                """;

        // Run parser
        parser.parse(new BufferedReader(new StringReader(script)));

        parser.addModule(new ShareableReservedTest("shareable1", "commonWord", "anotherOne"));
        parser.addModule(new ShareableReservedTest("shareable2", "commonWord", "anotherOne"));

        // Unhappy test cases
        // parser.addModule(new BasicLambdaModule("definitions",
        // line -> System.out.println("DEF → " + line), "defs"));
        //
        // parser.addModule(new BasicLambdaModule("definitions2",
        // line -> System.out.println("DEF → " + line), "def"));
        //
        // parser.addModule(new BasicLambdaModule("exclusive",
        // line -> System.out.println("DEF → " + line), "commonWord", "anotherOne"));
    }
}
