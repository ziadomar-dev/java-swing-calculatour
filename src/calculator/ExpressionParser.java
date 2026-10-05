package calculator;

class ExpressionParser {

    private final String text;
    private final Calculator calculator;
    private int pos = 0;

    ExpressionParser(String text, Calculator calculator) {
        this.text = text;
        this.calculator = calculator;
    }

    double parse() {
        double value = parseExpression();
        if (pos < text.length()) {
            throw syntaxError();
        }
        return value;
    }

    private double parseExpression() {
        double value = parseTerm();
        while (true) {
            char c = peek();
            if (c == '+') {
                pos++;
                value = calculator.add(value, parseTerm());
            } else if (c == '−') {
                pos++;
                value = calculator.subtract(value, parseTerm());
            } else {
                return value;
            }
        }
    }

    private double parseTerm() {
        double value = parseUnary();
        while (true) {
            char c = peek();
            if (c == '×') {
                pos++;
                value = calculator.multiply(value, parseUnary());
            } else if (c == '÷') {
                pos++;
                value = calculator.divide(value, parseUnary());
            } else {
                return value;
            }
        }
    }

    private double parseUnary() {
        char c = peek();
        if (c == '−') {
            pos++;
            return -parseUnary();
        }
        if (c == '+') {
            pos++;
            return parseUnary();
        }
        return parsePostfix();
    }

    private double parsePostfix() {
        double value = parsePrimary();
        while (true) {
            char c = peek();
            if (c == '%') {
                pos++;
                value = calculator.percent(value);
            } else if (c == '²') {
                pos++;
                value = calculator.square(value);
            } else {
                return value;
            }
        }
    }

    private double parsePrimary() {
        char c = peek();

        if (c == '(') {
            pos++;
            double value = parseExpression();
            if (peek() != ')') {
                throw syntaxError();
            }
            pos++;
            return value;
        }

        if (c == '√') {
            pos++;
            return calculator.squareRoot(parsePrimary());
        }

        if ((c >= '0' && c <= '9') || c == '.') {
            return parseNumber();
        }

        throw syntaxError();
    }

    private double parseNumber() {
        int start = pos;

        while (pos < text.length()
                && ((text.charAt(pos) >= '0' && text.charAt(pos) <= '9') || text.charAt(pos) == '.')) {
            pos++;
        }

        if (peek() == 'E') {
            pos++;
            if (peek() == '-' || peek() == '−') {
                pos++;
            }
            while (pos < text.length() && text.charAt(pos) >= '0' && text.charAt(pos) <= '9') {
                pos++;
            }
        }

        try {
            return Double.parseDouble(text.substring(start, pos).replace('−', '-'));
        } catch (NumberFormatException ex) {
            throw syntaxError();
        }
    }

    private char peek() {
        return pos < text.length() ? text.charAt(pos) : '\0';
    }

    private ArithmeticException syntaxError() {
        return new ArithmeticException("Invalid expression");
    }
}
