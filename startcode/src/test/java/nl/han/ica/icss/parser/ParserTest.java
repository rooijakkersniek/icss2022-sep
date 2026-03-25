package nl.han.ica.icss.parser;

import org.junit.jupiter.api.Test;

import static nl.han.ica.icss.parser.TestHelper.parseTestFile;
import static org.junit.jupiter.api.Assertions.*;

import nl.han.ica.icss.ast.*;

import java.io.*;

class ParserTest {

	@Test
	void testParseLevel0() throws IOException {

		AST sut = parseTestFile("level0.icss");
		AST exp = Fixtures.uncheckedLevel0();
		assertEquals(exp,sut);
	}
	@Test
	void testParseLevel1() throws IOException {

		AST sut = parseTestFile("level1.icss");
		AST exp = Fixtures.uncheckedLevel1();
		assertEquals(exp,sut);
	}
	@Test
	void testParseLevel2() throws IOException {

		AST sut = parseTestFile("level2.icss");
		AST exp = Fixtures.uncheckedLevel2();
		assertEquals(exp,sut);
	}
	@Test
	void testParseLevel3() throws IOException {

		AST sut = parseTestFile("level3.icss");
		AST exp = Fixtures.uncheckedLevel3();
		assertEquals(exp,sut);
	}
}
