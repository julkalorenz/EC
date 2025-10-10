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
     * @param costs Array representing costs associated with each point.

     */

    private int[][] distanceMatrix;
    private int[] costs;

    public GenericSolver(int[][] distanceMatrix, int[] costs) {
        this.distanceMatrix = distanceMatrix;
        this.costs = costs;
    }

    /** Method to be implemented by subclasses to solve the problem.
     *
     * @param startX Starting X coordinate.
     * @param startY Starting Y coordinate.
     * @return Solution object representing the solution found.
     */
    public abstract Solution getSolution(int startX, int startY);

    public int getScore(Solution solution) {
        int totalDistance = solution.getTotalDistance();
        int totalCost = solution.getTotalCost();

        return totalDistance + totalCost;
    }


    /**
     * TODO: Consider implementing here non-abstract run experiment
     * TODO: Consider implementing here non-abstract time tracking
     */

}
