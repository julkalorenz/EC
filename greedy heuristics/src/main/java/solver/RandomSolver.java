package main.java.solver;

import java.util.List;

import main.java.models.Solution;
import main.java.utils.CSVParser;
import main.java.models.Node;

public class RandomSolver extends GenericSolver {

    public RandomSolver(int[][] distanceMatrix, int[][] objectiveMatrix, int[] costs, List<Node> nodes) {
        super(distanceMatrix, objectiveMatrix, costs, nodes, "Random");
    }

    @Override
    public Solution getSolution(int startNodeID) {
        int totalNodes = getNodes().size();
        int nodesInCycle = (int) Math.ceil(totalNodes / 2.0);

        boolean[] selected = new boolean[totalNodes];
        int[] cycle = new int[nodesInCycle + 1]; // array of ids of size nodesInCycle + 1 (to return to start)

        int count = 0;

        while (count < nodesInCycle) {
            int randomIndex = (int) (Math.random() * totalNodes);
            if (!selected[randomIndex]) {
                selected[randomIndex] = true;
                cycle[count] = randomIndex;
                count++;
            }
        }
        // ensure the cycle starts and ends at the same node
        cycle[nodesInCycle] = cycle[0];
        return new Solution(getNodes(), getObjectiveMatrix(), getDistanceMatrix(), getCosts(), cycle, getMethodName());
    }
}
