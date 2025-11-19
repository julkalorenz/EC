package main.java.solver;


import main.java.models.Node;
import main.java.models.Solution;
import main.java.solver.LocalSearchSolver;
import main.java.utils.CSVParser;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class IteratedLocalSearchSolver extends GenericSolver {

    public IteratedLocalSearchSolver(int[][] distanceMatrix, int[][] objectiveMatrix, int[] costs, List<Node> nodes) {
        super(distanceMatrix, objectiveMatrix, costs, nodes, "Iterated Local Search");
    }

    public Solution perturbSolution(Solution solution) {
        // choose few pairs for edge exchange
        int[] path = solution.getPath();
        int size = path.length - 1;
        for (int i = 0; i < 7; i++) {
            int pos1 = (int) (Math.random() * size);
            int pos2 = (int) (Math.random() * size);
            while (
                    pos2 == pos1 ||
                    pos2 == (pos1 + 1) % size || // adjacent next
                    pos2 == (pos1 - 1 + size) % size // adjacent previous
            ) {
                pos2 = (int) (Math.random() * size);
            }

            if (pos1 > pos2) {
                int temp = pos1;
                pos1 = pos2;
                pos2 = temp;
            }

            int startNode1 = path[pos1];
            int startNode2 = path[pos2];

            int[] newPath = Arrays.copyOf(path, path.length);
            int left = pos1 + 1;
            int right = pos2;

            while (left < right) {
                int temp = newPath[left];
                newPath[left] = newPath[right];
                newPath[right] = temp;
                left++;
                right--;
            }
            path = newPath;
        }

        return new Solution(
                getNodes(),
                getObjectiveMatrix(),
                getDistanceMatrix(),
                getCosts(),
                path,
                getMethodName()
        );
    }

    @Override
    public Solution getSolution(int startNodeID) {
        // get intial solution using LS
        // then perturb and improve iteratively
        Set<Integer> allNodeIDs = getNodes().stream().map(Node::getId).collect(Collectors.toSet());

        LocalSearchSolver lsSolver = new LocalSearchSolver(
                this.getDistanceMatrix(),
                this.getObjectiveMatrix(),
                this.getCosts(),
                this.getNodes(),
                "Steepest",
                "Edge",
                "Random"
        );
        Solution currentSolution = lsSolver.getSolution(startNodeID);
        Solution bestSolution = currentSolution;
        System.out.println("Initial solution score: " + bestSolution.getScore());
        currentSolution = perturbSolution(currentSolution);

        System.out.println("Preturbed solution score: " + currentSolution.getScore());
        int[] nodesInPath = currentSolution.getPath();

        Solution newSolution = lsSolver.steepestLocalSearch(currentSolution, allNodeIDs);
        System.out.println("Improved solution score: " + newSolution.getScore());


        return null;
    }

    public static void main(String[] args) {
        String dataset = "TSPA";
        CSVParser parser = new CSVParser("src/main/data/" + dataset + ".csv", ";");
        int[][] distanceMatrix = parser.getDistanceMatrix();
        int[][] objectiveMatrix = parser.getObjectiveMatrix();
        List<Node> nodes = parser.getNodes();
        int[] costs = nodes.stream().mapToInt(Node::getCost).toArray();
        GenericSolver ilsSolver = new IteratedLocalSearchSolver(
                distanceMatrix,
                objectiveMatrix,
                costs,
                nodes
        );
        Solution solution = ilsSolver.getSolution(0);

    }

}


