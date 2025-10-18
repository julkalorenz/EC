package main.java.solver;

import main.java.models.Node;
import main.java.models.Solution;

import main.java.utils.CSVParser;

import java.util.List;

public class NN1Solver extends GenericSolver{
    public NN1Solver(int[][] distanceMatrix, int[][] objectiveMatrix, int[] costs, List<Node> nodes) {
        super(distanceMatrix, objectiveMatrix, costs, nodes, "Nearest Neighbor at end");
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
        int targetNodesCount = (int) Math.ceil((getDistanceMatrix().length - 1) / 2.0);
        int nodeCount = 1;
        int [] path = new int[targetNodesCount + 1];

        boolean[] visited = new boolean[getDistanceMatrix().length];

        visited[startNodeID] = true;

        path[0] = startNodeID;
        path[targetNodesCount] = startNodeID; // End node is the start node

        int currentNode = startNodeID;
        while (nodeCount < targetNodesCount) {
            int nextNode = findNearestNeighbor(currentNode, visited);

            path[nodeCount] = nextNode;
            visited[nextNode] = true;
            currentNode = nextNode;
            nodeCount++;
        }

        return new Solution(getNodes(), getObjectiveMatrix(), getDistanceMatrix(), getCosts(), path, getMethodName());

    }

}
