package main.java.solver;

import main.java.models.Solution;

public class MSLSSolver extends GenericSolver{

    public MSLSSolver(int[][] distanceMatrix, int[][] objectiveMatrix, int[] costs, java.util.List<main.java.models.Node> nodes) {
        super(distanceMatrix, objectiveMatrix, costs, nodes, "MSLS");
    }

    @Override
    public Solution getSolution(int startNodeID) {
        float bestScore = Float.MAX_VALUE;
        Solution bestSoltuion = null;


        for (int i = 0; i < 200; i++) {

            // Consider switching to delta solver if fixed
            GenericSolver LSSolver = new LocalSearchSolver(getDistanceMatrix(),
                    getObjectiveMatrix(),
                    getCosts(),
                    getNodes(),
                    "Steepest",
                    "Edge",
                    "Random");
            Solution currentSolution = LSSolver.getSolution(startNodeID);

            // Evaluate the improved solution
            float currentScore = currentSolution.getScore();

            // Update the best solution found so far
            if (currentScore < bestScore) {
                bestScore = currentScore;
                bestSoltuion = currentSolution;
            }
        }

        return bestSoltuion;
    }

}
