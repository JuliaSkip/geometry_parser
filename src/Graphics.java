import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

class DrawingPanel extends JPanel {
    private final ArrayList<Geometry> geometries;
    public static final int GRID_SPACING = 40;
    private static final Color GRID_COLOR = new Color(220, 220, 220);
    private int mouseX = -1;
    private int mouseY = -1;

    public DrawingPanel(ArrayList<Geometry> geometries) {
        this.geometries = geometries;
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawCartesianGrid(g);
        for (Geometry geometry : geometries) {
            geometry.draw(g);
        }
        drawMouseCoordinates(g);
    }

    private void drawCartesianGrid(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;

        // Draw grid lines
        g.setColor(GRID_COLOR);
        for (int x = centerX; x < width; x += GRID_SPACING) {
            g.drawLine(x, 0, x, height); // Right half
            g.drawLine(centerX - (x - centerX), 0, centerX - (x - centerX), height); // Left half
        }
        for (int y = centerY; y < height; y += GRID_SPACING) {
            g.drawLine(0, y, width, y); // Bottom half
            g.drawLine(0, centerY - (y - centerY), width, centerY - (y - centerY)); // Top half
        }

        // Draw x and y axes with arrows
        g.setColor(Color.BLACK);
        g.drawLine(centerX, 0, centerX, height); // Y-axis
        g.drawLine(0, centerY, width, centerY); // X-axis
        drawArrow(g, width - 10, centerY, width - 20, centerY - 5); // X-axis arrow (positive)
        drawArrow(g, width - 10, centerY, width - 20, centerY + 5);
        drawArrow(g, centerX, 10, centerX - 5, 20); // Y-axis arrow (positive)
        drawArrow(g, centerX, 10, centerX + 5, 20);

        // Draw labels only on the axes
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 10));

        // Label origin
        g.drawString("0", centerX + 5, centerY - 5);

        // Label positive and negative X-axis
        for (int x = centerX + GRID_SPACING; x < width; x += GRID_SPACING) {
            int gridX = (x - centerX) / GRID_SPACING;
            g.drawString(String.valueOf(gridX), x + 2, centerY - 2);
            g.drawString(String.valueOf(-gridX), centerX - (x - centerX) + 2, centerY - 2);
        }

        // Label positive and negative Y-axis
        for (int y = centerY + GRID_SPACING; y < height; y += GRID_SPACING) {
            int gridY = -(y - centerY) / GRID_SPACING;
            g.drawString(String.valueOf(gridY), centerX + 2, y - 2);
            g.drawString(String.valueOf(-gridY), centerX + 2, centerY - (y - centerY) - 2);
        }
    }

    private void drawArrow(Graphics g, int x1, int y1, int x2, int y2) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(x1, y1, x2, y2);
        g2d.dispose();
    }

    private void drawMouseCoordinates(Graphics g) {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        if (mouseX >= 0 && mouseY >= 0) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            int gridX = (mouseX - centerX) / GRID_SPACING;
            int gridY = -(mouseY - centerY) / GRID_SPACING;
            g.drawString("Mouse: (" + gridX + ", " + gridY + ")", mouseX + 10, mouseY - 10);
        }
    }
}

interface Geometry {
    void draw(Graphics g);
}

class PointGeometry implements Geometry {
    private final String name;
    private final int x, y;
    private static final int POINT_SIZE = 10;
    private static final Color POINT_COLOR = Color.BLUE;
    private static final Color BORDER_COLOR = Color.BLACK;
    private static final Color SHADOW_COLOR = new Color(150, 150, 150, 150);
    private static final Font NAME_FONT = new Font("SansSerif", Font.BOLD, 14);

    public PointGeometry(String name, int x, int y) {
        int centerX = 400; // Assuming a 800x800 panel
        int centerY = 286; // Adjusted center to match your previous implementation
        this.x = centerX + x * DrawingPanel.GRID_SPACING; // X coordinate remains the same
        this.y = centerY - y * DrawingPanel.GRID_SPACING; // Invert the Y-coordinate
        this.name = name;
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(SHADOW_COLOR);
        g2d.fill(new Ellipse2D.Double(x - POINT_SIZE / 2 + 3, y - POINT_SIZE / 2 + 3, POINT_SIZE, POINT_SIZE));

        g2d.setColor(BORDER_COLOR);
        g2d.drawOval(x - POINT_SIZE / 2, y - POINT_SIZE / 2, POINT_SIZE, POINT_SIZE);
        g2d.setColor(POINT_COLOR);
        g2d.fillOval(x - POINT_SIZE / 2 + 1, y - POINT_SIZE / 2 + 1, POINT_SIZE - 2, POINT_SIZE - 2);

        g2d.setFont(NAME_FONT);
        g2d.setColor(Color.BLACK);
        g2d.drawString(name, x + POINT_SIZE / 2 + 5, y + 5);
    }
}

class SegmentGeometry implements Geometry {
    private final int x1, y1, x2, y2;

    public SegmentGeometry(int x1, int y1, int x2, int y2) {
        int centerX = 400; // Assuming a 800x800 panel
        int centerY = 286; // Adjusted center to match your previous implementation
        this.x1 = centerX + x1 * DrawingPanel.GRID_SPACING;
        this.y1 = centerY - y1 * DrawingPanel.GRID_SPACING;
        this.x2 = centerX + x2 * DrawingPanel.GRID_SPACING;
        this.y2 = centerY - y2 * DrawingPanel.GRID_SPACING;
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(Color.GREEN);
        g.drawLine(x1, y1, x2, y2);
    }
}