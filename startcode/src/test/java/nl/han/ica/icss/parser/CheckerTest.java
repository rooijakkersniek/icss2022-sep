package nl.han.ica.icss.parser;

import nl.han.ica.icss.ast.AST;
import nl.han.ica.icss.ast.ASTNode;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.checker.Checker;
import nl.han.ica.icss.transforms.Evaluator;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class CheckerTest {
    AST parseTestFile(String resource) throws IOException {

        //Open test file to parse
        ClassLoader classLoader = this.getClass().getClassLoader();

        InputStream inputStream = classLoader.getResourceAsStream(resource);
        CharStream charStream = CharStreams.fromStream(inputStream);
        ICSSLexer lexer = new ICSSLexer(charStream);

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        ICSSParser parser = new ICSSParser(tokens);
        parser.setErrorHandler(new BailErrorStrategy());

        //Setup collection of the parse error messages
        BaseErrorListener errorListener = new BaseErrorListener() {
            private String message;
            public void syntaxError(Recognizer<?,?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                message = msg;
            }
            public String toString() {
                return message;
            }
        };
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        //Parse & extract AST
        ASTListener listener = new ASTListener();
        try {
            ParseTree parseTree = parser.stylesheet();
            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(listener, parseTree);
        } catch(ParseCancellationException e) {
            fail(errorListener.toString());
        }

        return listener.getAST();
    }

    @Test
    void checkerWrongPropertyType() throws IOException {
        AST sut = parseTestFile("checker_test_wrong_property_type.icss");
        Checker checker = new Checker();
        checker.check(sut);
        assertTrue(hasErrors(sut.root));

        String errorMessage = getFirstErrorMessage(sut.root);
        assertEquals("ERROR: Invalid type for property width", errorMessage);
    }

    @Test
    void checkerScalarInMultiplyOperation() throws IOException {
        AST sut = parseTestFile("checker_test_scalar_multiply_operation.icss");
        Checker checker = new Checker();
        checker.check(sut);
        assertTrue(hasErrors(sut.root));
    }

    @Test
    void checkerNonExistentVariable() throws IOException {
        AST sut = parseTestFile("checker_usage_of_non_existent_variable.icss");
        Checker checker = new Checker();
        checker.check(sut);
        assertTrue(hasErrors(sut.root));

        String errorMessage = getFirstErrorMessage(sut.root);
        assertEquals("ERROR: Invalid type for property color", errorMessage);
    }

    @Test
    void evaluatorTestOperation() throws IOException {
        AST sut = parseTestFile("evaluator_test_declaration.icss");

        Evaluator evaluator = new Evaluator();
        evaluator.apply(sut);

        var stylerule = sut.root.getChildren().get(0);
        var simplifiedDeclaration = stylerule.getChildren().get(1);
        var simplifiedExpression = simplifiedDeclaration.getChildren().get(1);

        System.out.println(simplifiedExpression);

        assertEquals(30, ((PixelLiteral) simplifiedExpression).value);
    }

    @Test
    void evaluatorTestIfElseFalse() throws IOException {
        AST sut = parseTestFile("evaluator_test_statement_false.icss");

        Evaluator evaluator = new Evaluator();
        evaluator.apply(sut);

        var stylerule = sut.root.getChildren().get(0);
        System.out.println("stylerule: " + stylerule);

        var simplifiedStatement = stylerule.getChildren().get(1);
        System.out.println("statement: " + simplifiedStatement);

        var simplifiedExpression = simplifiedStatement.getChildren().get(1);
        System.out.println("expression: " + simplifiedExpression);

        assertEquals("#000000", ((ColorLiteral) simplifiedExpression).value);
    }

    @Test
    void evaluatorTestIfElseTrue() throws IOException {
        AST sut = parseTestFile("evaluator_test_statement_true.icss");

        Evaluator evaluator = new Evaluator();

        var stylerulebefore = sut.root.getChildren().get(0);
        System.out.println("stylerule before: " + stylerulebefore);

        evaluator.apply(sut);

        var stylerule = sut.root.getChildren().get(0);
        System.out.println("stylerule after : " + stylerule);

        var simplifiedStatement = stylerule.getChildren().get(1);
        System.out.println("statement after : " + simplifiedStatement);

        var simplifiedExpression = simplifiedStatement.getChildren().get(1);
        System.out.println("expression after: " + simplifiedExpression);

        assertEquals("#ff0000", ((ColorLiteral) simplifiedExpression).value);
    }

    @Test
    void evaluatorTestFull() throws IOException {
        AST sut = parseTestFile("evaluator_test_full.icss");
        Evaluator evaluator = new Evaluator();

        System.out.println("stylesheet before: " + sut);

        evaluator.apply(sut);

        var stylerule = sut.root.getChildren().get(0);
        System.out.println("stylerule after : " + stylerule);
        var simplifiedStatement = stylerule.getChildren().get(1);
        System.out.println("statement after : " + simplifiedStatement);
        var simplifiedExpression = simplifiedStatement.getChildren().get(1);
        System.out.println("expression after: " + simplifiedExpression);

        assertEquals(200, ((PixelLiteral) simplifiedExpression).value);
    }

    private boolean hasErrors(ASTNode node) {
        if (node.getError() != null) {
            return true;
        }
        for (ASTNode child : node.getChildren()) {
            if (hasErrors(child)) {
                return true;
            }
        }
        return false;
    }

    private String getFirstErrorMessage(ASTNode node) {
        if (node.getError() != null) {
            return node.getError().toString();
        }
        for (ASTNode child : node.getChildren()) {
            String childError = getFirstErrorMessage(child);
            if (childError != null) {
                return childError;
            }
        }
        return null;
    }
}
