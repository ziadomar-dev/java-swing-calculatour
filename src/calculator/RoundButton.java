package calculator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class RoundButton extends JButton {

    private final Color base;
    private boolean hovering = false;

    RoundButton(String text, Color background, Color foreground) {
        super(text);
        this.base = background;

        setForeground(foreground);
        setFont(new Font("SansSerif", Font.PLAIN, 22));
        setFocusable(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovering = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovering = false;
                repaint();
            }
        });
    }

    private static Color shift(Color c, int amount) {
        return new Color(
                Math.max(0, Math.min(255, c.getRed() + amount)),
                Math.max(0, Math.min(255, c.getGreen() + amount)),
                Math.max(0, Math.min(255, c.getBlue() + amount))
        );
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color fill = base;
        if (getModel().isPressed()) {
            fill = shift(base, -25);
        } else if (hovering) {
            fill = shift(base, 20);
        }

        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
        g2.dispose();

        super.paintComponent(g);
    }
}
