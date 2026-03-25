package nl.han.ica.icss.parser;

import nl.han.ica.icss.ast.AST;
import nl.han.ica.icss.ast.ASTNode;
import nl.han.ica.icss.checker.Checker;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static nl.han.ica.icss.parser.TestHelper.parseTestFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CheckerTest {
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
