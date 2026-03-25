package nl.han.ica.icss.parser;

import nl.han.ica.icss.ast.AST;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.transforms.Evaluator;
import org.junit.jupiter.api.Test;
import java.io.IOException;

import static nl.han.ica.icss.parser.TestHelper.parseTestFile;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EvaluatorTest {
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
}
