package hello;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HelloWorldConfigurationTests {

	@Test
	public void testAddition() {
		Expression e = new ExpressionBuilder("1+1").build();
		double result = e.evaluate();
		assertEquals(2.0, result, 0.0001);
	}

//	@Test
//	public void testSubtractionError() {
//		Expression e = new ExpressionBuilder("5-1").build();
//		double result = e.evaluate();
//		assertEquals(2.0, result, 0.0001);
//	}
}