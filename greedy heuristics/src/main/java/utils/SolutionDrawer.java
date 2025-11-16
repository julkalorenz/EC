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
    private int minCost, maxCost;

    private static final int MIN_RADIUS = 4;
    private static final int MAX_RADIUS = 14;

    public SolutionDrawer(List<Node> nodesList, int[] cycleNodes) {
        this.nodesList = nodesList;
        this.cycleNodes = cycleNodes;
        this.setPreferredSize(new Dimension(panelWidth, panelHeight));
        calculateBounds();
        calculateCostRange();
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

    private void calculateCostRange() {
        minCost = Integer.MAX_VALUE;
        maxCost = Integer.MIN_VALUE;
        for (Node node : nodesList) {
            int cost = node.getCost();
            if (cost < minCost) minCost = cost;
            if (cost > maxCost) maxCost = cost;
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

    private int radiusForCost(int cost) {
        if (maxCost <= minCost) {
            return (MIN_RADIUS + MAX_RADIUS) / 2;
        }
        double t = (double) (cost - minCost) / (double) (maxCost - minCost);
        int r = (int) Math.round(MIN_RADIUS + t * (MAX_RADIUS - MIN_RADIUS));
        if (r < MIN_RADIUS) r = MIN_RADIUS;
        if (r > MAX_RADIUS) r = MAX_RADIUS;
        return r;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        // Improve visual quality
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(Color.GRAY);
        for (Node node : nodesList) {
            int x = scaleX(node.getX());
            int y = scaleY(node.getY());
            int r = radiusForCost(node.getCost());
            g2d.fillOval(x - r, y - r, 2 * r, 2 * r);
        }

        g2d.setColor(Color.RED);
        for (int i = 0; i < cycleNodes.length - 1; i++) {
            Node from = nodesList.get(cycleNodes[i]);
            Node to = nodesList.get(cycleNodes[i + 1]);

            int x1 = scaleX(from.getX());
            int y1 = scaleY(from.getY());
            int x2 = scaleX(to.getX());
            int y2 = scaleY(to.getY());

            int rFrom = radiusForCost(from.getCost());
            g2d.fillOval(x1 - rFrom, y1 - rFrom, 2 * rFrom, 2 * rFrom);
            g2d.drawLine(x1, y1, x2, y2);
        }
    }

}