package calculator;

public class CalculatorSmokeTest {
    public static void main(String[] args) {
        Calculator c = new Calculator();
        assert Math.abs(c.add(2, 3) - 5.0) < 1e-9;
        assert Math.abs(c.multiply(4, 2.5) - 10.0) < 1e-9;
        assert Math.abs(c.percent(25) - 0.25) < 1e-9;
        assert Math.abs(new ExpressionParser("2+3×4", c).parse() - 14.0) < 1e-9;
        assert Math.abs(new ExpressionParser("√(81)+2²", c).parse() - 13.0) < 1e-9;
        boolean caught = false;
        try {
            new ExpressionParser("10÷0", c).parse();
        } catch (ArithmeticException ex) {
            caught = true;
        }
        assert caught : "Division by zero should fail";
        System.out.println("Smoke tests passed.");
    }
}
