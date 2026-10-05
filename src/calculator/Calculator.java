package calculator;

class Calculator {

    public double add(double num1, double num2) {
        return num1 + num2;
    }

    public double subtract(double num1, double num2) {
        return num1 - num2;
    }

    public double multiply(double num1, double num2) {
        return num1 * num2;
    }

    public double divide(double num1, double num2) {
        if (num2 == 0) {
            throw new ArithmeticException("Cannot divide by zero");
        }
        return num1 / num2;
    }

    public double percent(double value) {
        return value / 100;
    }

    public double square(double value) {
        return value * value;
    }

    public double squareRoot(double value) {
        if (value < 0) {
            throw new ArithmeticException("Invalid input");
        }
        return Math.sqrt(value);
    }
}
