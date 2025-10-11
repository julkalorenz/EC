package main.java.solver;

import main.java.models.Solution;

public abstract class GenericSolver {
    /**
     * Generic solver class to be extended by specific heuristic implementations.
     * It holds the distance matrix and costs array.
     *
     * Consider Random Solver as extension of this class too
     *
     * @param distanceMatrix 2D array representing distances between points.
     * @param objectiveMatrix 2D array representing distance between points + cost of both nodes.
     * @param costs Array representing costs associated with each point.

     */

    private int[][] distanceMatrix;
    private int[][] objectiveMatrix;
    private int[] costs;

    public GenericSolver(int[][] distanceMatrix,int[][] objectiveMatrix, int[] costs) {
        this.distanceMatrix = distanceMatrix;
        this.objectiveMatrix = objectiveMatrix;
        this.costs = costs;
    }

    public int[][] getDistanceMatrix() {
        return distanceMatrix;
    }
    public int[][] getObjectiveMatrix() {
        return objectiveMatrix;
    }
    public int[] getCosts() {
        return costs;
    }

    /** Method to be implemented by subclasses to solve the problem.
     * @param startNodeID The ID of the starting node for the solution.
     *
     * @return Solution object representing the solution found.
     */
    public abstract Solution getSolution(int startNodeID);

}
