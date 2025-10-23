package main.java.models;

import main.java.utils.SolutionDrawer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;

public class Solution {
    /**
     * I think it may be a good idea to have a consistent Solution class
     * This way we can create methods such as calculate solution score
     *
     */

    private List<Node> nodes;
    private int[][] objectiveMatrix;
    private int[][] distanceMatrix;
    private int[] costs;
    private int[] path; // Array of node IDs (indexes in nodesList) IMPORTANT: should start and end with the same node
    private String methodName;
    private int iterationCount;

    public Solution(List<Node> nodes, int[][] objectiveMatrix, int[][] distanceMatrix,
                    int[] costs, int[] path, String methodName) {
        this.nodes = nodes;
        this.objectiveMatrix = objectiveMatrix;
        this.distanceMatrix = distanceMatrix;
        this.costs = costs;
        this.path = path;
        this.methodName = methodName;
    }

    public Solution(List<Node> nodes, int[][] objectiveMatrix, int[][] distanceMatrix,
                    int[] costs, int[] path, String methodName, int iterationCount) {
        this(nodes, objectiveMatrix, distanceMatrix, costs, path, methodName);
        this.iterationCount = iterationCount;
    }

    public int getTotalDistance() {

        int totalDistance = 0;
        for (int i = 0; i < path.length - 1; i++) {
            totalDistance += distanceMatrix[path[i]][path[i + 1]]; // End node is the start node so thats enough, no need to add dist between path[-1] and path[0]
        }

        return totalDistance;
    }

    public int getTotalObjective() {

        int totalObjective = 0;
        for (int i = 0; i < path.length - 1; i++) {
            totalObjective += objectiveMatrix[path[i]][path[i + 1]];
        }

        return totalObjective;
    }

    public int getTotalCost() {
        int totalCost = 0;
        for (int node : path) {
            totalCost += costs[node];
        }
        totalCost -= costs[path[0]]; // Subtract the cost of the starting/ending node as it's counted twice
        return totalCost;
    }

    public int getScore() {
        return getTotalDistance() + getTotalCost();
    }

    public int[] getPath() {
        return path;
    }

    public int getIterationCount() {
        return iterationCount;
    }

    public void setIterationCount(int iterationCount) {
        this.iterationCount = iterationCount;
    }

    public void displaySolution() {
        SolutionDrawer drawer = new SolutionDrawer(nodes, path);
        JFrame frame = new JFrame("Solution");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(drawer);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void saveAsImage(String filePath) {
        SolutionDrawer drawer = new SolutionDrawer(nodes, path);

        Dimension size = drawer.getPreferredSize();
        int originalWidth = size.width;
        int originalHeight = size.height;

        int topPadding = 60;
        int totalHeight = originalHeight + topPadding;

        drawer.setSize(originalWidth, originalHeight);
        drawer.doLayout();

        BufferedImage image = new BufferedImage(originalWidth, totalHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, originalWidth, topPadding);

        g2d.setColor(Color.WHITE);
        Font font = new Font("Arial", Font.BOLD, 16);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics(font);

        String distanceText = "Total Distance: " + getTotalDistance();
        String objectiveText = "Total Objective: " + getTotalObjective();
        int padding = 10;
        int lineHeight = 20;

        g2d.drawString(distanceText, padding, padding + lineHeight - 4);
        g2d.drawString(objectiveText, padding, padding + 2 * lineHeight - 4);

        if (methodName != null && !methodName.isEmpty()) {
            String methodText = "Method: " + methodName;
            int textWidth = fm.stringWidth(methodText);
            int xRight = originalWidth - textWidth - padding;
            int y = padding + lineHeight - 4;
            g2d.drawString(methodText, xRight, y);
        }

        g2d.translate(0, topPadding);
        drawer.paint(g2d);
        g2d.dispose();

        try {
            File outputFile = new File(filePath);
            outputFile.getParentFile().mkdirs(); // ensure directory exists
            ImageIO.write(image, "png", outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void savePath(String filePath) {
        File file = new File(filePath);
        file.getParentFile().mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (int nodeId : path) {
                writer.write(String.valueOf(nodeId));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
