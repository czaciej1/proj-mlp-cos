import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class Canvas extends JPanel {

    private static final int TILE_COUNT = 8;
    private static final int PIXELS_PER_TILE = 8;
    private static final int GRID_SIZE = TILE_COUNT * PIXELS_PER_TILE;

    private final boolean[][] pixels = new boolean[GRID_SIZE][GRID_SIZE];
    private final int[][] tileCounts = new int[TILE_COUNT][TILE_COUNT];

    private int pixelSize;
    private int offsetX;
    private int offsetY;

    public Canvas() {

        MouseAdapter mouse = new MouseAdapter() {
            public void mousePressed(MouseEvent e) { draw(e); }
            public void mouseDragged(MouseEvent e) { draw(e); }
        };

        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    private void updateGeometry() {

        int size = Math.min(getWidth(), getHeight());
        pixelSize = size / GRID_SIZE;

        int canvasSize = pixelSize * GRID_SIZE;

        offsetX = (getWidth() - canvasSize) / 2;
        offsetY = (getHeight() - canvasSize) / 2;
    }
    
    private void draw(MouseEvent e) {

        updateGeometry();

        int cx = (e.getX() - offsetX) / pixelSize;
        int cy = (e.getY() - offsetY) / pixelSize;

        if (cx < 0 || cy < 0 || cx >= GRID_SIZE || cy >= GRID_SIZE) return;

        boolean draw = SwingUtilities.isLeftMouseButton(e);
        boolean erase = SwingUtilities.isRightMouseButton(e);

        if (!draw && !erase) return;

        // brush shape (3x3 cross)

        int[][] brush = {
                {0,0}, {-1,0}, {1,0}, {0,-1}, {0,1} // cross
                // 3x3 square option:
                // {-1,-1},{0,-1},{1,-1},{-1,0},{0,0},{1,0},{-1,1},{0,1},{1,1}
        };

        for (int[] b : brush) {

            int x = cx + b[0];
            int y = cy + b[1];

            if (x < 0 || y < 0 || x >= GRID_SIZE || y >= GRID_SIZE) continue;

            if (draw && !pixels[y][x]) {

                pixels[y][x] = true;
                tileCounts[y / PIXELS_PER_TILE][x / PIXELS_PER_TILE]++;

            } else if (erase && pixels[y][x]) {

                pixels[y][x] = false;
                tileCounts[y / PIXELS_PER_TILE][x / PIXELS_PER_TILE]--;
            }
        }

        repaint();
    }
//    private void draw(MouseEvent e) {
//
//        updateGeometry();
//
//        int x = (e.getX() - offsetX) / pixelSize;
//        int y = (e.getY() - offsetY) / pixelSize;
//
//        if (x < 0 || y < 0 || x >= GRID_SIZE || y >= GRID_SIZE) return;
//
//        if (!pixels[y][x]) {
//
//            pixels[y][x] = true;
//
//            int tileX = x / PIXELS_PER_TILE;
//            int tileY = y / PIXELS_PER_TILE;
//
//            tileCounts[tileY][tileX]++;
//
//            repaint();
//        }
//    }
   
    
    public String exportAsLabeledString(String label) {

        double[] vector = getInputVector();

        StringBuilder sb = new StringBuilder();
        sb.append(label);

        for (double v : vector) {
            sb.append(" ").append(v);
        }

        return sb.toString();
    }

    public void resetCanvas() {

        for (int y = 0; y < GRID_SIZE; y++)
            for (int x = 0; x < GRID_SIZE; x++)
                pixels[y][x] = false;

        for (int y = 0; y < TILE_COUNT; y++)
            for (int x = 0; x < TILE_COUNT; x++)
                tileCounts[y][x] = 0;

        repaint();
    }

    // tile density calc

    public double[][] sampleTileDensity() {

        double[][] density = new double[TILE_COUNT][TILE_COUNT];
        double max = PIXELS_PER_TILE * PIXELS_PER_TILE;

        for (int y = 0; y < TILE_COUNT; y++)
            for (int x = 0; x < TILE_COUNT; x++)
                density[y][x] = tileCounts[y][x] / max;

        return density;
    }

    //vector output for MLP

    public double[] getInputVector() {

        double[] vector = new double[TILE_COUNT * TILE_COUNT];
        double max = PIXELS_PER_TILE * PIXELS_PER_TILE;

        int i = 0;

        for (int y = 0; y < TILE_COUNT; y++)
            for (int x = 0; x < TILE_COUNT; x++)
                vector[i++] = tileCounts[y][x] / max;

        return vector;
    }

    //export image (for manually adding to the learnset, saving to string in a file might be more optimal tho #IDK)

    public BufferedImage exportImage(int resolution) {

        BufferedImage img = new BufferedImage(
                GRID_SIZE * resolution,
                GRID_SIZE * resolution,
                BufferedImage.TYPE_BYTE_GRAY
        );

        Graphics2D g = img.createGraphics();

        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {

                g.setColor(pixels[y][x] ? Color.BLACK : Color.WHITE);

                g.fillRect(
                        x * resolution,
                        y * resolution,
                        resolution,
                        resolution
                );
            }
        }

        g.dispose();
        return img;
    }

    //rendering

    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        updateGeometry();

        Graphics2D g2 = (Graphics2D) g;

        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {

                int px = offsetX + x * pixelSize;
                int py = offsetY + y * pixelSize;

                g2.setColor(pixels[y][x] ? Color.BLACK : Color.WHITE);
                g2.fillRect(px, py, pixelSize, pixelSize);

                g2.setColor(Color.LIGHT_GRAY);
                g2.drawRect(px, py, pixelSize, pixelSize);
            }
        }

        g2.setColor(Color.RED);

        for (int i = 0; i <= GRID_SIZE; i += PIXELS_PER_TILE) {

            int pos = i * pixelSize;

            g2.drawLine(offsetX + pos, offsetY,
                    offsetX + pos, offsetY + GRID_SIZE * pixelSize);

            g2.drawLine(offsetX, offsetY + pos,
                    offsetX + GRID_SIZE * pixelSize, offsetY + pos);
        }
    }
}