package redactedrice.modularparser.test;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import redactedrice.modularparser.alias.DefaultAliasSupporter;
import redactedrice.modularparser.alias.DefaultScopedAliasParser;
import redactedrice.modularparser.comment.DefaultMutliLineCommentLineModifier;
import redactedrice.modularparser.comment.DefaultSingleLineCommentLineModifier;
import redactedrice.modularparser.core.BaseModule;
import redactedrice.modularparser.core.ModularParser;
import redactedrice.modularparser.lineformer.DefaultContinuerLineModifier;
import redactedrice.modularparser.lineformer.DefaultGroupingLineModifier;
import redactedrice.modularparser.lineformer.DefaultLineFormerSupporter;
import redactedrice.modularparser.lineparser.DefaultLambdaParser;
import redactedrice.modularparser.lineparser.DefaultLineParserSupport;
import redactedrice.modularparser.literal.DefaultBoolLiteralParser;
import redactedrice.modularparser.literal.DefaultChainingChainableLiteralParser;
import redactedrice.modularparser.literal.DefaultCharLiteralParser;
import redactedrice.modularparser.literal.DefaultLiteralSupporter;
import redactedrice.modularparser.literal.DefaultNumberLiteralParser;
import redactedrice.modularparser.literal.SimpleObjectLiteralParser;
import redactedrice.modularparser.literal.named.DefaultNamedLiteralSupporter;
import redactedrice.modularparser.literal.named.DefaultScopedNamedLiteralParser;
import redactedrice.modularparser.log.DefaultConsoleLogHandler;
import redactedrice.modularparser.log.DefaultLogSupporter;
import redactedrice.modularparser.log.LogSupporter.LogLevel;
import redactedrice.modularparser.reserved.DefaultReservedWordSupporter;
import redactedrice.modularparser.reserved.ReservedWordSupporter.ReservedType;
import redactedrice.modularparser.reserved.WordReserver;
import redactedrice.modularparser.scope.DefaultScopeSupporter;

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
        ModularParser parser = new ModularParser();

        DefaultLineFormerSupporter reader = new DefaultLineFormerSupporter();
        parser.addModule(reader);
        parser.addModule(new DefaultLineParserSupport());
        parser.addModule(new DefaultLiteralSupporter());
        DefaultLogSupporter logger = new DefaultLogSupporter();
        parser.addModule(logger);
        parser.addModule(new DefaultNamedLiteralSupporter());
        parser.addModule(new DefaultAliasSupporter());
        parser.addModule(new DefaultReservedWordSupporter());
        parser.addModule(new DefaultConsoleLogHandler());

        DefaultScopeSupporter scope = new DefaultScopeSupporter("BasicScopeHandler", true);
        scope.pushScope("global");
        scope.pushScope("file");
        parser.addModule(scope);

        parser.addModule(
                new DefaultSingleLineCommentLineModifier("BasicCSingleLineCommentModule", "//"));
        parser.addModule(new DefaultSingleLineCommentLineModifier(
                "BasicPythonSingleLineCommentModule", "#"));
        parser.addModule(
                new DefaultMutliLineCommentLineModifier("BasicMutliLineCommentModule", "/*", "*/"));
        parser.addModule(
                new DefaultGroupingLineModifier("BasicParenthesisModule", "(", ")", false));
        parser.addModule(new DefaultContinuerLineModifier("BasicLineContinuerModule", "\\", true));

        parser.addModule(new DefaultChainingChainableLiteralParser("->", parser));
        parser.addModule(new DefaultNumberLiteralParser());
        parser.addModule(new DefaultCharLiteralParser());
        parser.addModule(new DefaultBoolLiteralParser());
        parser.addModule(new SimpleObjectLiteralParser());

        parser.addModule(new DefaultScopedNamedLiteralParser("BasicVarHandler", true, "variable"));
        parser.addModule(
                new DefaultScopedNamedLiteralParser("BasicConstHandler", false, "constant"));
        parser.addModule(new DefaultScopedAliasParser());

        parser.addModule(new DefaultLambdaParser("TestPrintHandler",
                line -> logger.log(LogLevel.INFO, "DefaultLambdaParser: Print: " + line.substring(8)), "println"));
        parser.configureModules();

        // Test script as a multiline string
        String script = """
                # This is a comment (
                /* This is (
                   a block comment */
                alias greet = println "Hello"
                file alias greet2 = println "Hello2"
                global alias greet2 = println "Hello3"
                greet
                variable num = 42
                variable so1 = SimpleObject(intVal 5, boolVal true, strVal "test")
                variable so2 = SimpleObject(5, true, "test with space")
                variable so3 = SimpleObject(strVal "test with space", intVal 6, boolVal true)
                variable so4 = SimpleObject(5, strVal test no quotes, boolVal true)
                variable so5 = SimpleObject(5) -> SimpleObject(4) ->  SimpleObject(3) -> 5
                // variable so6 = SimpleObject(boolVal true, 5)
                //variable so6 = SimpleObject()
                //variable so6 = SimpleObject( 5, 5, 5, 5)
                variable so7 = SimpleObject(5, strVal "test")
                variable so8 = SimpleObject(5, true)
                num = 43
                2num = 43
                file constant num2 = 42.3
                global num3 = 41L   // ERROR not defined
                global variable num3 = 42L // global
                num3 = 43           // global
                file num3 = 44L     // error not defined
                file variable num3 = 45L // file def
                file num3 = 46L     // file
                num3 = 47           // file
                global num3 = 48L   // global
                file variable num4 = 42.3d
                variable num5 = 42i
                num5 = 42.3e3
                global variable num5 =
                variable num7 = \\
                  42e3
                variable num8 = 42e3L
                variable num9 = num
                constant num9 = 1
                global variable str = "This is a string test"
                variable ch = '\t'
                global constant bool1 = TRUE
                constant bool2 = f
                constant bool2 = t  // error
                bool2 = f 			// error
                variable bar = "true ->
                   and something"
                // Some comment  (
                alias greet = println "Hello 2"
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
        reader.setReader(new BufferedReader(new StringReader(script)));
        parser.parse();

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
