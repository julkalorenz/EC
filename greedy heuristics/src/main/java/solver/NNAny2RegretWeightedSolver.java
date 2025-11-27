package main.java.solver;

import main.java.models.InsertionInfo;
import main.java.models.Node;
import main.java.models.Solution;

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

    public Solution completeSolution(int[] incompletePath) {
        int targetNodesCount = (int) Math.ceil((getDistanceMatrix().length - 1) / 2.0);

        // remove the last node
        ArrayList<Integer> path = new ArrayList<>();
        for (int i = 0; i < incompletePath.length; i++) {
            if (i == incompletePath.length - 1 && incompletePath[0] == incompletePath[i]) {
                // Last node is same as starting node → skip it
                break;
            }
            path.add(incompletePath[i]);
        }

        boolean[] visited = new boolean[getDistanceMatrix().length];
        for (int nodeId : path) {
            visited[nodeId] = true;
        }

        int nodeCount = path.size();

        while (nodeCount < targetNodesCount) {
            InsertionInfo insertion = findNeighborAndPosition(path, visited);
            int nextNode = insertion.nodeId;
            int position = insertion.index;

            path.add(position, nextNode);
            visited[nextNode] = true;
            nodeCount++;
        }
        path.add(path.getFirst()); // End node is the start node

        int[] finalPath = path.stream().mapToInt(i -> i).toArray();
        return new Solution(getNodes(), getObjectiveMatrix(), getDistanceMatrix(), getCosts(), finalPath, getMethodName());
    }
}
