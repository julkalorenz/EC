package main.java.solver;

import main.java.models.Node;
import main.java.models.Solution;

import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

public class GreedyCycleSolver extends GenericSolver{
    public GreedyCycleSolver(int[][] distanceMatrix, int[][] objectiveMatrix, int[] costs, List<Node> nodes) {
        super(distanceMatrix, objectiveMatrix, costs, nodes, "GreedyCycle");
    }

    public int findNearestNeighbor(int currentNode, boolean[] visited) {
        int nearestNeighbor = -1;
        int minScore = Integer.MAX_VALUE;

        for (int i = 0; i < getObjectiveMatrix().length; i++) {
            if (!visited[i] && getObjectiveMatrix()[currentNode][i] < minScore) {
                minScore = getObjectiveMatrix()[currentNode][i];
                nearestNeighbor = i;
            }
        }
        return nearestNeighbor;
    }

    @Override
    public Solution getSolution(int startNodeID) {
        int targetNodesCount = (int) Math.ceil((getDistanceMatrix().length) / 2.0);
        boolean[] visited = new boolean[getDistanceMatrix().length];
        Set<Integer> unvisitedIds = new HashSet<>();
        for (int i = 0; i < visited.length; i++) {
            unvisitedIds.add(i);
        }

        visited[startNodeID] = true;
        unvisitedIds.remove(startNodeID);
        int nearestNode = findNearestNeighbor(startNodeID, visited);
        visited[nearestNode] = true;
        unvisitedIds.remove(nearestNode);

        int nodeCount = 2;

        List<Integer> path = new ArrayList<>();
        path.add(startNodeID);
        path.add(nearestNode);
        path.add(startNodeID); // to complete the cycle

        while (nodeCount < targetNodesCount) {
            int bestNode = -1;
            int bestPosition = -1;
            int bestIncrement = Integer.MAX_VALUE;

            for (int candidate: unvisitedIds) {
                for (int i = 0; i < path.size() - 1; i++) {
                    int curr = path.get(i);
                    int next = path.get(i + 1);
                    int increment = getObjectiveMatrix()[curr][candidate]
                            + getObjectiveMatrix()[candidate][next]
                            - getObjectiveMatrix()[curr][next];

                    if (increment < bestIncrement) {
                        bestIncrement = increment;
                        bestNode = candidate;
                        bestPosition = i + 1; // insert after current node
                    }
                }
            }
            path.add(bestPosition, bestNode);
            visited[bestNode] = true;
            unvisitedIds.remove(bestNode);
            nodeCount++;

        }
        int[] finalPath = path.stream().mapToInt(Integer::intValue).toArray();
        return new Solution(getNodes(), getObjectiveMatrix(), getDistanceMatrix(), getCosts(), finalPath, getMethodName());
    }

}
