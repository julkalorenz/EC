package main.java.solver;

import java.util.List;
import main.java.utils.CSVParser;
import main.java.models.Node;

public class RandomSolver {

//    choose 50% of the nodes for the cycle - if odd ceil
//    distance between nodes = Euclidean distance rounded mathematically
//    start from a random node and choose a random node until there are 50% of nodes selected
    public static void main(String[] args) {
        String filePath = "src/main/data/TSPA.csv";
        String delimiter = ";";
        CSVParser parser = new CSVParser(filePath, delimiter);
        List<Node> data = parser.getNodes();
        RandomSolver randomSolver = new RandomSolver();
        int[] cycle = randomSolver.generateRandomSolution(data);

        int lineNo = 1;
        for (int row : cycle) {
            System.out.print("Line " + lineNo + ": ");
            System.out.print(row);
            System.out.println();
            lineNo++;
        }

    }

    public int[] generateRandomSolution(List<Node> nodes) {
        int totalNodes = nodes.size();
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
        return cycle;
    }

}
