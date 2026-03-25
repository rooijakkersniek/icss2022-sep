package nl.han.ica.icss.parser;

import nl.han.ica.icss.ast.AST;
import nl.han.ica.icss.generator.Generator;
import nl.han.ica.icss.transforms.Evaluator;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static nl.han.ica.icss.parser.TestHelper.parseTestFile;

public class GeneratorTest {
    @Test
    public void generatorTest() throws IOException {
        AST sut = parseTestFile("generator_test.icss");

        Evaluator evaluator = new Evaluator();
        Generator generator = new Generator();

        evaluator.apply(sut);
        String result = generator.generate(sut);

        System.out.println(result);
    }
}
