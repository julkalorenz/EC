package main.java.solver;

import main.java.models.InsertionInfo;
import main.java.models.Node;
import main.java.models.Solution;

import java.util.List;
import java.util.ArrayList;

public class NN2Solver extends GenericSolver{
    public NN2Solver(int[][] distanceMatrix, int[][] objectiveMatrix, int[] costs, List<Node> nodes) {
        super(distanceMatrix, objectiveMatrix, costs, nodes, "Nearest Neighbor Any");
    }

    protected InsertionInfo findNeighborAndPosition(ArrayList<Integer> currentPath, boolean[] visited) {
        int bestNode = -1;
        int bestPosition = -1;
        int minScore = Integer.MAX_VALUE;

        for (int node = 0; node < getObjectiveMatrix().length; node++) {
            if (!visited[node]) {
                for (int position = 0; position <= currentPath.size(); position++) {
                    int tempScore = 0;
                    if (position == 0){
                        tempScore += getObjectiveMatrix()[node][currentPath.getFirst()];
                        tempScore -= getCosts()[currentPath.getFirst()];
                        tempScore += getCosts()[node];
                    } else if (position == currentPath.size()) {
                        tempScore += getObjectiveMatrix()[currentPath.getLast()][node];

                    } else{
                        tempScore += getObjectiveMatrix()[currentPath.get(position - 1)][node];
                        tempScore += getObjectiveMatrix()[node][currentPath.get(position)];
                        tempScore -= getObjectiveMatrix()[currentPath.get(position - 1)][currentPath.get(position)];
                    }

                    if (tempScore < minScore) {
                        minScore = tempScore;
                        bestNode = node;
                        bestPosition = position;
                    }
                }
            }
        }
        return new InsertionInfo(bestPosition, bestNode);
    }

    @Override
    public Solution getSolution(int startNodeID) {
        int targetNodesCount = (int) Math.ceil((getDistanceMatrix().length - 1) / 2.0);
        int nodeCount = 1;

        ArrayList<Integer> path = new ArrayList<>();
        path.add(startNodeID);

        boolean[] visited = new boolean[getDistanceMatrix().length];
        visited[startNodeID] = true;

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
