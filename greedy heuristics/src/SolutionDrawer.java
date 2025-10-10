import javax.swing.*;
import java.awt.*;

public class SolutionDrawer extends JPanel {

//    TODO represent the cost of a node on the graph
    private int[][] nodes;
    private int[][] cycleNodes;

    // Scaling variables
    private int panelWidth = 1000;
    private int panelHeight = 800;
    private int margin = 40;

    private int minX, maxX, minY, maxY;

    public SolutionDrawer(int[][] nodes, int[][] cycleNodes) {
        this.nodes = nodes;
        this.cycleNodes = cycleNodes;
        this.setPreferredSize(new Dimension(panelWidth, panelHeight));
        calculateBounds();
    }

    private void calculateBounds() {
        minX = Integer.MAX_VALUE;
        maxX = Integer.MIN_VALUE;
        minY = Integer.MAX_VALUE;
        maxY = Integer.MIN_VALUE;

        for (int[] node : nodes) {
            if (node[0] < minX) minX = node[0];
            if (node[0] > maxX) maxX = node[0];
            if (node[1] < minY) minY = node[1];
            if (node[1] > maxY) maxY = node[1];
        }
    }

    private int scaleX(int x) {
        double scale = (double) (panelWidth - 2 * margin) / (maxX - minX);
        return (int) ((x - minX) * scale + margin);
    }

    private int scaleY(int y) {
        double scale = (double) (panelHeight - 2 * margin) / (maxY - minY);
        // Y is inverted because screen Y grows down
        return panelHeight - (int) ((y - minY) * scale + margin);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        int radius = 5;

        g2d.setColor(Color.GRAY);
        for (int[] node : nodes) {
            int x = scaleX(node[0]);
            int y = scaleY(node[1]);
            g2d.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
        }

        g2d.setColor(Color.RED);
        for (int i = 0; i < cycleNodes.length - 1; i++) {
            int x1 = scaleX(cycleNodes[i][0]);
            int y1 = scaleY(cycleNodes[i][1]);
            int x2 = scaleX(cycleNodes[i + 1][0]);
            int y2 = scaleY(cycleNodes[i + 1][1]);

            g2d.fillOval(x1 - radius, y1 - radius, 2 * radius, 2 * radius);
            g2d.drawLine(x1, y1, x2, y2);
        }

    }

    public static void main(String[] args) {
        String filePath = "data/TSPA.csv";
        String delimiter = ";";
        CSVParser parser = new CSVParser(filePath, delimiter);
        int[][] data = parser.readCSV();

        RandomSolution randomSolution = new RandomSolution();
        int[][] cycle = randomSolution.generateRandomSolution(data);

        JFrame frame = new JFrame("TSP Solution");
        SolutionDrawer drawer = new SolutionDrawer(data, cycle);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(drawer);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
