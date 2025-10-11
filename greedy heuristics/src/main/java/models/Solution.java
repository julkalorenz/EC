package main.java.models;

import main.java.utils.SolutionDrawer;

import javax.swing.*;
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

    public Solution(List<Node> nodes, int[][] objectiveMatrix, int[][] distanceMatrix, int[] costs, int[] path) {
        this.nodes = nodes;
        this.objectiveMatrix = objectiveMatrix;
        this.distanceMatrix = distanceMatrix;
        this.costs = costs;
        this.path = path;
    }

    public int getTotalDistance() {

        int totalDistance = 0;
        for (int i = 0; i < path.length - 1; i++) {
            totalDistance += distanceMatrix[path[i]][path[i + 1]]; // End node is the start node so thats enough, no need to add dist between path[-1] and path[0]
        }

        return totalDistance;
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
    public void displaySolution() {
        SolutionDrawer drawer = new SolutionDrawer(nodes, path);
        JFrame frame = new JFrame("Solution");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(drawer);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
