package main.java.utils;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import main.java.models.Node;
import main.java.solver.RandomSolver;

public class SolutionDrawer extends JPanel {

    private List<Node> nodesList; // List of Node objects
    private int[] cycleNodes;     // Array of node IDs (indexes in nodesList) IMPORTANT: should start and end with the same node

    private int panelWidth = 1000;
    private int panelHeight = 800;
    private int margin = 40;

    private int minX, maxX, minY, maxY;

    public SolutionDrawer(List<Node> nodesList, int[] cycleNodes) {
        this.nodesList = nodesList;
        this.cycleNodes = cycleNodes;
        this.setPreferredSize(new Dimension(panelWidth, panelHeight));
        calculateBounds();
    }

    private void calculateBounds() {
        minX = Integer.MAX_VALUE;
        maxX = Integer.MIN_VALUE;
        minY = Integer.MAX_VALUE;
        maxY = Integer.MIN_VALUE;

        for (Node node : nodesList) {
            if (node.getX() < minX) minX = node.getX();
            if (node.getX() > maxX) maxX = node.getX();
            if (node.getY() < minY) minY = node.getY();
            if (node.getY() > maxY) maxY = node.getY();
        }
    }

    private int scaleX(int x) {
        double scale = (double) (panelWidth - 2 * margin) / (maxX - minX);
        return (int) ((x - minX) * scale + margin);
    }

    private int scaleY(int y) {
        double scale = (double) (panelHeight - 2 * margin) / (maxY - minY);
        return panelHeight - (int) ((y - minY) * scale + margin); // Inverted Y
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        int radius = 6;

        g2d.setColor(Color.GRAY);
        for (Node node : nodesList) {
            int x = scaleX(node.getX());
            int y = scaleY(node.getY());
            g2d.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
        }

        g2d.setColor(Color.RED);
        for (int i = 0; i < cycleNodes.length - 1; i++) {
            Node from = nodesList.get(cycleNodes[i]);
            Node to = nodesList.get(cycleNodes[i + 1]);

            int x1 = scaleX(from.getX());
            int y1 = scaleY(from.getY());
            int x2 = scaleX(to.getX());
            int y2 = scaleY(to.getY());

            g2d.fillOval(x1 - radius, y1 - radius, 2 * radius, 2 * radius);
            g2d.drawLine(x1, y1, x2, y2);
        }
    }

    public static void main(String[] args) {
        CSVParser parser = new CSVParser("src/main/data/TSPA.csv", ";");
        List<Node> nodeList = parser.getNodes();
        RandomSolver randomSolver = new RandomSolver();
        int[] cycle = randomSolver.generateRandomSolution(nodeList);

        JFrame frame = new JFrame("Solution");
        SolutionDrawer drawer = new SolutionDrawer(nodeList, cycle);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(drawer);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
