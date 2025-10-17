package main.java.solver;

import main.java.models.Node;
import main.java.models.Solution;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GreedyCycle2RegretWeightedSolver extends GenericSolver {

    private double regretWeight;
    private double scoreWeight;

    public GreedyCycle2RegretWeightedSolver(
            int[][] distanceMatrix,
            int[][] objectiveMatrix,
            int[] costs,
            List<Node> nodes,
            double regretWeight,
            double scoreWeight
    ) {
        super(distanceMatrix, objectiveMatrix, costs, nodes, "Greedy Cycle 2-Regret Weighted");
        this.regretWeight = regretWeight;
        this.scoreWeight = scoreWeight;
    }

    public int findNearestNeighbor(int currentNode, Set<Integer> unvisitedNodes  ) {
        int nearestNeighbor = -1;
        int minScore = Integer.MAX_VALUE;

        for (int i = 0; i < getObjectiveMatrix().length; i++) {
            if (unvisitedNodes.contains(i) && (getDistanceMatrix()[currentNode][i] + getCosts()[i]) < minScore) {
                minScore = getDistanceMatrix()[currentNode][i] + getCosts()[i];
                nearestNeighbor = i;
            }
        }
        return nearestNeighbor;
    }

    @Override
    public Solution getSolution(int startNodeID) {
        int targetNodesCount = (int) Math.ceil((getDistanceMatrix().length) / 2.0);
        Set<Integer> unvisitedIds = new HashSet<>();
        for (int i = 0; i < getDistanceMatrix().length; i++) {
            unvisitedIds.add(i);
        }

        unvisitedIds.remove(startNodeID);
        int nearestNode = findNearestNeighbor(startNodeID, unvisitedIds);
        unvisitedIds.remove(nearestNode);

        int nodeCount = 2;

        List<Integer> path = new ArrayList<>();
        path.add(startNodeID);
        path.add(nearestNode);
        path.add(startNodeID); // to complete the cycle

        while (nodeCount < targetNodesCount) {
            int bestNode = -1;
            int bestPosition = -1;
            double minWeightedScore = Integer.MAX_VALUE;

            for (int candidate: unvisitedIds) {
                // for each node not in cycle
                // find the best insertion position and the 2nd best
                int bestInsertPosition1 = -1;
                int bestInsertPosition2 = -1;
                int increment1 = Integer.MAX_VALUE;
                int increment2 = Integer.MAX_VALUE;

                for (int i = 0; i < path.size() - 1; i++) {
                    int curr = path.get(i);
                    int next = path.get(i + 1);
                    int increment = (getDistanceMatrix()[curr][candidate] + getCosts()[candidate])
                            + (getDistanceMatrix()[candidate][next] + getCosts()[next])
                            - (getDistanceMatrix()[curr][next] + getCosts()[next]);

                    if (increment < increment1) {
                        increment1 = increment;
                        bestInsertPosition1 = i + 1; // insert after current node
                    } else if (increment < increment2) {
                        increment2 = increment;
                        bestInsertPosition2 = i + 1;
                    }
                }
                // compute regret
                int regret = increment2 - increment1;
                double weightedScore = (scoreWeight * increment1) - (regretWeight * regret);
                if (weightedScore < minWeightedScore) {
                    // update the best node as the node with max regret
                    minWeightedScore = weightedScore;
                    bestNode = candidate;
                    bestPosition = bestInsertPosition1;
                }
            }
            path.add(bestPosition, bestNode);
            unvisitedIds.remove(bestNode);
            nodeCount++;

        }
        int[] finalPath = path.stream().mapToInt(Integer::intValue).toArray();
        return new Solution(getNodes(), getObjectiveMatrix(), getDistanceMatrix(), getCosts(), finalPath, getMethodName());
    }

}
