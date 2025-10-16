package main.java.solver;

import main.java.models.InsertionInfo;
import main.java.models.Node;
import main.java.models.Solution;

import java.util.List;
import java.util.ArrayList;

public class NNAny2RegretSolver extends NN2Solver{


    public NNAny2RegretSolver(int[][] distanceMatrix, int[][] objectiveMatrix, int[] costs, List<Node> nodes) {
        super(distanceMatrix, objectiveMatrix, costs, nodes);
        super.setMethodName("Nearest Neighbor Any 2-Regret");
    }

    @Override
    protected InsertionInfo findNeighborAndPosition(ArrayList<Integer> currentPath, boolean[] visited) {
        int bestNode = -1;
        int bestPosition = -1;
        int maxRegret = Integer.MIN_VALUE;
        int minScore = Integer.MAX_VALUE;

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
            if (regret > maxRegret || (regret == maxRegret && tempScore1 < minScore)) {
                maxRegret = regret;
                minScore = tempScore1;
                bestNode = node;
                bestPosition = tempPosition;
            }

        }
        return new InsertionInfo(bestPosition, bestNode);
    }

}
