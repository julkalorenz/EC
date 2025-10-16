package main.java.solver;

import main.java.models.InsertionInfo;
import main.java.models.Node;

import java.util.List;
import java.util.ArrayList;

public class NNAny2RegretWeightedSolver extends NN2Solver{
    private final double regretWeight;
    private final double scoreWeight;

    private static final double DEFAULT_REGRET_WEIGHT = 0.5;
    private static final double DEFAULT_SCORE_WEIGHT = 0.5;

    public NNAny2RegretWeightedSolver(
            int[][] distanceMatrix,
            int[][] objectiveMatrix,
            int[] costs,
            List<Node> nodes,
            double regretWeight,
            double scoreWeight
    ) {
        super(distanceMatrix, objectiveMatrix, costs, nodes);
        this.regretWeight = regretWeight;
        this.scoreWeight = scoreWeight;
        super.setMethodName("Nearest Neighbor Any 2-Regret Weighted");
    }

    public NNAny2RegretWeightedSolver(
            int[][] distanceMatrix,
            int[][] objectiveMatrix,
            int[] costs,
            List<Node> nodes
    ) {
        this(
                distanceMatrix,
                objectiveMatrix,
                costs,
                nodes,
                DEFAULT_REGRET_WEIGHT,
                DEFAULT_SCORE_WEIGHT // Passing the default values
        );
    }

    @Override
    protected InsertionInfo findNeighborAndPosition(ArrayList<Integer> currentPath, boolean[] visited) {
        int bestNode = -1;
        int bestPosition = -1;

        double minWeightedScore = Integer.MAX_VALUE;

        int tempScore1, tempScore2, tempPosition;

        for (int node = 0; node < getObjectiveMatrix().length; node++) {
            tempScore1 = Integer.MAX_VALUE;
            tempScore2 = Integer.MAX_VALUE;
            tempPosition = -1;

            if (!visited[node]) {
                for (int position = 0; position <= currentPath.size(); position++) {
                    int tempScore = 0;

                    if (position == 0){
                        tempScore += getDistanceMatrix()[node][currentPath.getFirst()];
                    } else if (position == currentPath.size()) {
                        tempScore += getDistanceMatrix()[currentPath.getLast()][node];

                    } else{
                        tempScore += getDistanceMatrix()[currentPath.get(position - 1)][node];
                        tempScore += getDistanceMatrix()[node][currentPath.get(position)];
                        tempScore -= getDistanceMatrix()[currentPath.get(position - 1)][currentPath.get(position)];
                    }

                    tempScore += getCosts()[node];

                    if (tempScore < tempScore1) {
                        tempScore2 = tempScore1;
                        tempScore1 = tempScore;
                        tempPosition = position;

                    } else if (tempScore < tempScore2) {
                        tempScore2 = tempScore;
                    }
                }
            }

            int regret = tempScore2 - tempScore1;

            double weightedScore = (scoreWeight * tempScore1) - (regretWeight * regret);


            if (weightedScore < minWeightedScore ) {
                minWeightedScore = weightedScore;
                bestNode = node;
                bestPosition = tempPosition;
            }

        }
        return new InsertionInfo(bestPosition, bestNode);
    }
}
