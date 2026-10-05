package calculator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class Main extends JFrame implements ActionListener {

    private static final String OPERATORS = "+−×÷";
    private static final int TEXT_WIDTH = 320;

    private static final Color BACKGROUND = new Color(28, 28, 30);
    private static final Color DIGIT_COLOR = new Color(60, 60, 64);
    private static final Color FUNCTION_COLOR = new Color(92, 92, 98);
    private static final Color OPERATOR_COLOR = new Color(255, 149, 10);
    private static final Color EQUALS_COLOR = new Color(0, 122, 255);
    private static final Color CLEAR_COLOR = new Color(205, 65, 60);

    private final Calculator calculator = new Calculator();

    private final JLabel historyLabel = new JLabel(" ");
    private final JLabel displayLabel = new JLabel("0");

    private String expression = "";
    private String historyText = "";
    private String errorMessage = "";
    private boolean justEvaluated = false;
    private boolean hasError = false;

    public Main() {

        setTitle("Calculator");
        setSize(380, 640);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(BACKGROUND);
        setLayout(new BorderLayout());

        JPanel displayPanel = new JPanel(new GridLayout(2, 1));
        displayPanel.setBackground(BACKGROUND);
        displayPanel.setBorder(BorderFactory.createEmptyBorder(25, 20, 5, 20));
        displayPanel.setPreferredSize(new Dimension(0, 150));

        historyLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        historyLabel.setVerticalAlignment(SwingConstants.BOTTOM);
        historyLabel.setForeground(new Color(150, 150, 155));
        historyLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));

        displayLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        displayLabel.setVerticalAlignment(SwingConstants.CENTER);
        displayLabel.setForeground(Color.WHITE);
        displayLabel.setFont(new Font("SansSerif", Font.PLAIN, 48));

        displayPanel.add(historyLabel);
        displayPanel.add(displayLabel);
        add(displayPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(6, 4, 10, 10));
        buttonPanel.setBackground(BACKGROUND);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 20, 15));

        String[] buttons = {
                "C", "⌫", "(", ")",
                "%", "x²", "√", "÷",
                "7", "8", "9", "×",
                "4", "5", "6", "−",
                "1", "2", "3", "+",
                "±", "0", ".", "="
        };

        for (String text : buttons) {
            RoundButton button = new RoundButton(text, colorFor(text), Color.WHITE);
            button.setActionCommand(text);
            button.addActionListener(this);
            buttonPanel.add(button);
        }

        add(buttonPanel, BorderLayout.CENTER);

        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                handleKey(e.getKeyChar());
            }
        });

        refresh();
        setVisible(true);
        requestFocusInWindow();
    }

    private Color colorFor(String text) {
        switch (text) {
            case "C":
                return CLEAR_COLOR;
            case "=":
                return EQUALS_COLOR;
            case "+":
            case "−":
            case "×":
            case "÷":
                return OPERATOR_COLOR;
            case "⌫":
            case "(":
            case ")":
            case "%":
            case "x²":
            case "√":
            case "±":
                return FUNCTION_COLOR;
            default:
                return DIGIT_COLOR;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        handle(e.getActionCommand());
    }

    private void handleKey(char c) {

        if (c >= '\u0660' && c <= '\u0669') {
            c = (char) ('0' + (c - '\u0660'));
        }

        if (c >= '0' && c <= '9') {
            handle(String.valueOf(c));
            return;
        }

        switch (c) {
            case '+':
                handle("+");
                break;
            case '-':
                handle("−");
                break;
            case '*':
            case 'x':
            case 'X':
                handle("×");
                break;
            case '/':
                handle("÷");
                break;
            case '.':
            case ',':
                handle(".");
                break;
            case '(':
                handle("(");
                break;
            case ')':
                handle(")");
                break;
            case '%':
                handle("%");
                break;
            case '=':
            case '\n':
                handle("=");
                break;
            case '\b':
                handle("⌫");
                break;
            case 'c':
            case 'C':
            case '\u001b':   // Escape
            case '\u007f':   // Delete
                handle("C");
                break;
            default:
                break;
        }
    }

    private void handle(String cmd) {

        if (hasError) {
            clearAll();
            if (cmd.equals("C") || cmd.equals("⌫")) {
                refresh();
                return;
            }
        }

        // Pressing "=" twice should not do anything
        if (justEvaluated && cmd.equals("=")) {
            return;
        }

        if (justEvaluated) {
            boolean startsNewEntry = isDigit(cmd) || cmd.equals(".")
                    || cmd.equals("(") || cmd.equals("√");
            if (startsNewEntry) {
                expression = "";
            }
            justEvaluated = false;
        }

        switch (cmd) {
            case "C":
                clearAll();
                break;
            case "⌫":
                backspace();
                break;
            case "=":
                evaluate();
                break;
            case ".":
                inputDecimal();
                break;
            case "+":
            case "−":
            case "×":
            case "÷":
                inputOperator(cmd.charAt(0));
                break;
            case "(":
                inputOpenParen();
                break;
            case ")":
                inputCloseParen();
                break;
            case "%":
                inputPostfix('%');
                break;
            case "x²":
                inputPostfix('²');
                break;
            case "√":
                inputSqrt();
                break;
            case "±":
                toggleSign();
                break;
            default:
                if (isDigit(cmd)) {
                    inputDigit(cmd.charAt(0));
                }
                break;
        }

        refresh();
    }

    private boolean isDigit(String s) {
        return s.length() == 1 && s.charAt(0) >= '0' && s.charAt(0) <= '9';
    }

    private void clearAll() {
        expression = "";
        historyText = "";
        errorMessage = "";
        justEvaluated = false;
        hasError = false;
    }

    private void backspace() {
        if (expression.isEmpty()) {
            return;
        }
        if (expression.endsWith("√(")) {
            expression = expression.substring(0, expression.length() - 2);
        } else {
            expression = expression.substring(0, expression.length() - 1);
        }
    }

    private boolean endsWithValue() {
        if (expression.isEmpty()) {
            return false;
        }
        char c = expression.charAt(expression.length() - 1);
        return (c >= '0' && c <= '9') || c == ')' || c == '%' || c == '²';
    }

    private int numberStart() {
        int i = expression.length();
        while (i > 0) {
            char c = expression.charAt(i - 1);
            if ((c >= '0' && c <= '9') || c == '.') {
                i--;
            } else {
                break;
            }
        }
        return i;
    }

    private void inputDigit(char d) {
        if (endsWithValue() && !Character.isDigit(expression.charAt(expression.length() - 1))) {
            expression += "×";          
        }

        int start = numberStart();
        String token = expression.substring(start);

        if (token.length() >= 15) {
            return;                     
        }

        if (token.equals("0")) {
            expression = expression.substring(0, start) + d; 
        } else {
            expression += d;
        }
    }

    private void inputDecimal() {
        String token = expression.substring(numberStart());

        if (token.contains(".")) {
            return;
        }

        if (token.isEmpty()) {
            if (endsWithValue()) {
                expression += "×";
            }
            expression += "0.";
        } else {
            expression += ".";
        }
    }

    private void inputOperator(char op) {
        if (expression.isEmpty()) {
            if (op == '−') {
                expression = "−";       
            }
            return;
        }

        char last = expression.charAt(expression.length() - 1);

        if (last == '(') {
            if (op == '−') {
                expression += op;       
            }
            return;
        }

        if (OPERATORS.indexOf(last) >= 0) {
            if (expression.length() == 1) {
                return;
            }
            
            expression = expression.substring(0, expression.length() - 1) + op;
            return;
        }

        expression += op;
    }

    private void inputOpenParen() {
        if (endsWithValue()) {
            expression += "×";
        }
        expression += "(";
    }

    private void inputCloseParen() {
        int open = 0;
        for (char c : expression.toCharArray()) {
            if (c == '(') open++;
            if (c == ')') open--;
        }
        if (open > 0 && endsWithValue()) {
            expression += ")";
        }
    }

    private void inputPostfix(char symbol) {
        if (endsWithValue()) {
            expression += symbol;
        }
    }

    private void inputSqrt() {
        if (endsWithValue()) {
            expression += "×";
        }
        expression += "√(";
    }

    private void toggleSign() {
        int s = numberStart();
        if (s == expression.length()) {
            return;                    
        }

        if (s == 0) {
            expression = "−" + expression;
            return;
        }

        char prev = expression.charAt(s - 1);

        if (prev == '−') {
            boolean unary = s == 1 || "+−×÷(".indexOf(expression.charAt(s - 2)) >= 0;
            expression = expression.substring(0, s - 1)
                    + (unary ? "" : "+")
                    + expression.substring(s);
        } else if (prev == '+') {
            expression = expression.substring(0, s - 1) + "−" + expression.substring(s);
        } else {
            expression = expression.substring(0, s) + "−" + expression.substring(s);
        }
    }

    private String prepare(String text) {
        String t = text;

        while (!t.isEmpty() && OPERATORS.indexOf(t.charAt(t.length() - 1)) >= 0) {
            t = t.substring(0, t.length() - 1);
        }

        int open = 0;
        for (char c : t.toCharArray()) {
            if (c == '(') open++;
            if (c == ')') open--;
        }

        StringBuilder sb = new StringBuilder(t);
        for (int i = 0; i < open; i++) {
            sb.append(')');
        }
        return sb.toString();
    }

    private void evaluate() {
        if (expression.isEmpty()) {
            return;
        }

        String prepared = prepare(expression);

        try {
            double value = new ExpressionParser(prepared, calculator).parse();
            String result = format(value);

            historyText = prepared + " =";
            expression = result;
            justEvaluated = true;

        } catch (ArithmeticException ex) {
            hasError = true;
            errorMessage = ex.getMessage();
        }
    }

    private String format(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new ArithmeticException("Result is too large");
        }

        BigDecimal bd = new BigDecimal(value)
                .round(new MathContext(12, RoundingMode.HALF_UP))
                .stripTrailingZeros();

        if (bd.signum() == 0) {
            return "0";
        }

        double abs = Math.abs(value);
        String s = (abs >= 1e12 || abs < 1e-6) ? bd.toString() : bd.toPlainString();

        s = s.replace("E+", "E");

        if (s.startsWith("-")) {
            s = "−" + s.substring(1);
        }
        return s;
    }

    private String preview() {
        if (expression.isEmpty()) {
            return "";
        }
        try {
            String result = format(new ExpressionParser(prepare(expression), calculator).parse());
            return result.equals(expression) ? "" : "= " + pretty(result);
        } catch (ArithmeticException ex) {
            return "";
        }
    }

    private String pretty(String text) {
        return text.replaceAll("(?<=[0-9.)%²])([+−×÷])", " $1 ").trim();
    }

    private void refresh() {
        if (hasError) {
            fit(displayLabel, "Error", 48, 24, Font.PLAIN);
            fit(historyLabel, errorMessage, 18, 12, Font.PLAIN);
            return;
        }

        String main = expression.isEmpty() ? "0" : pretty(expression);
        fit(displayLabel, main, 48, 20, Font.PLAIN);

        String top = justEvaluated ? pretty(historyText) : preview();
        fit(historyLabel, top.isEmpty() ? " " : top, 18, 12, Font.PLAIN);
    }


    private void fit(JLabel label, String text, int maxSize, int minSize, int style) {
        int size = maxSize;
        Font font = new Font("SansSerif", style, size);
        FontMetrics fm = label.getFontMetrics(font);

        while (size > minSize && fm.stringWidth(text) > TEXT_WIDTH) {
            size -= 2;
            font = new Font("SansSerif", style, size);
            fm = label.getFontMetrics(font);
        }

        String shown = text;
        while (shown.length() > 1 && fm.stringWidth(shown) > TEXT_WIDTH) {
            shown = shown.substring(1);
        }
        if (!shown.equals(text)) {
            shown = "…" + shown.substring(1);
        }

        label.setFont(font);
        label.setText(shown);
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main());
    }
}
