package main.java.models;

public class Solution {
    /**
     * I think it may be a good idea to have a consistent Solution class
     * This way we can create methods such as calculate solution score
     *
     * IMPORTANT: We need to agree on a consistent way to represent the path
     */

    private int[][] distanceMatrix;
    private int[] costs;
    private int[] path;

    public Solution(int[][] distanceMatrix, int[] costs, int[] path) {
        this.distanceMatrix = distanceMatrix;
        this.costs = costs;
        this.path = path;
    }

    public int getTotalDistance() {

        int totalDistance = 0;
        for (int i = 0; i < path.length - 1; i++) {
            totalDistance += distanceMatrix[path[i]][path[i + 1]];
        }

        totalDistance += distanceMatrix[path[path.length - 1]][path[0]]; // Return to start
        return totalDistance;
    }

    public int getTotalCost() {
        int totalCost = 0;
        for (int node : path) {
            totalCost += costs[node];
        }
        return totalCost;
    }

}
