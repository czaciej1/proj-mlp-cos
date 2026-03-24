import javax.swing.*;
import java.awt.*;

public class PrevPanel extends JPanel {

    private double[][] data = new double[8][8];

    public void setData(double[][] data) {
        this.data = data;
        repaint();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int size = Math.min(getWidth(), getHeight());
        int cell = size / 8;

        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {

                float v = (float) data[y][x]; // 0 → 1

                g.setColor(new Color(1-v, 1-v, 1-v)); // grayscale
                g.fillRect(x * cell, y * cell, cell, cell);

                g.setColor(Color.BLACK);
                g.drawRect(x * cell, y * cell, cell, cell);
            }
        }
    }
}