import javax.swing.*;
import java.util.ArrayList;
public class Code {
    public static void main(String[] args) {       JFrame frame = new JFrame("Geometry Drawer");
       ArrayList<Geometry> geometries = new ArrayList<>();
       geometries.add(new SegmentGeometry(3,4,5,4));
       geometries.add(new SegmentGeometry(3,4,3,2));
       geometries.add(new SegmentGeometry(3,2,5,2));
       geometries.add(new SegmentGeometry(5,4,5,2));
       geometries.add(new PointGeometry("A1",5,4));
       geometries.add(new PointGeometry("B1",5,2));
       geometries.add(new PointGeometry("A",3,2));
       geometries.add(new PointGeometry("B",3,4));
       DrawingPanel drawingPanel = new DrawingPanel(geometries);
       frame.add(drawingPanel);
       frame.setSize(800, 600);
       frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       frame.setLocationRelativeTo(null);
       frame.setResizable(false);
       frame.setVisible(true);
    }
}
