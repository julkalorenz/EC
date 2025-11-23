package main.java.solver;


import main.java.models.Node;
import main.java.models.Solution;
import main.java.solver.LocalSearchSolver;
import main.java.utils.CSVParser;

import java.util.*;
import java.util.stream.Collectors;

public class IteratedLocalSearchSolver extends GenericSolver {

    private float stoppingTimeSeconds;
    private int totalLSRuns = 0;


    public IteratedLocalSearchSolver(
            int[][] distanceMatrix,
            int[][] objectiveMatrix,
            int[] costs,
            List<Node> nodes,
            float stoppingTimeSeconds
    ) {
        super(distanceMatrix, objectiveMatrix, costs, nodes, "Iterated Local Search");
        this.stoppingTimeSeconds = stoppingTimeSeconds;
    }

    public int getTotalLSRuns() {
        return totalLSRuns;
    }
    public void setTotalLSRuns(int totalLSRuns) {
        this.totalLSRuns = totalLSRuns;
    }

    public Solution perturbSolution(Solution solution, Set<Integer> nodeIDsInSolution, Set<Integer> allNodeIDs) {
        // choose few pairs for edge exchange
        int[] path = solution.getPath();
        int size = path.length - 1;
        for (int i = 0; i < 5; i++) {
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

        // node swap
        int posA = (int) (Math.random() * size);
        if (posA == 0) {
            posA = 1;
        }
        int nodeA = path[posA];
        Set<Integer> nodesOutside = new HashSet<>(allNodeIDs);
        nodesOutside.removeAll(nodeIDsInSolution);

        List<Integer> outsideList = new ArrayList<>(nodesOutside);
        int randomIndex = (int) (Math.random() * outsideList.size());
        int nodeB = outsideList.get(randomIndex);

        path[posA] = nodeB;
        nodeIDsInSolution.remove(nodeA);
        nodeIDsInSolution.add(nodeB);


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

        float bestScore = Float.MAX_VALUE;
        Solution bestSolution = null;
        LocalSearchSolver lsSolver = new LocalSearchSolver(
                this.getDistanceMatrix(),
                this.getObjectiveMatrix(),
                this.getCosts(),
                this.getNodes(),
                "Steepest",
                "Edge",
                "Random"
        );
        Set<Integer> allNodeIDs = getNodes().stream().map(Node::getId).collect(Collectors.toSet());
        Solution currentSolution = lsSolver.getSolution(startNodeID);
        bestSolution = currentSolution;
        bestScore = currentSolution.getScore();

        long startTime = System.nanoTime();
        while (true) {
            long currentTime = System.nanoTime();
            float elapsedTimeSeconds = (currentTime - startTime) / 1_000_000_000.0f;
            if (elapsedTimeSeconds >= stoppingTimeSeconds) {
                break;
            }

            Set<Integer> nodeIDsInSolution = Arrays.stream(currentSolution.getPath()).boxed().collect(Collectors.toSet());
            Solution perturbedSolution = perturbSolution(currentSolution, nodeIDsInSolution, allNodeIDs);
            currentSolution = lsSolver.steepestLocalSearch(perturbedSolution, allNodeIDs);

            float currentScore = currentSolution.getScore();
            if (currentScore < bestScore) {
                bestSolution = currentSolution;
                bestScore = currentScore;
            }
            totalLSRuns++;
        }
        return bestSolution;
    }

    public static void main(String[] args) {
        String dataset = "TSPB";
        CSVParser parser = new CSVParser("src/main/data/" + dataset + ".csv", ";");
        int[][] distanceMatrix = parser.getDistanceMatrix();
        int[][] objectiveMatrix = parser.getObjectiveMatrix();
        List<Node> nodes = parser.getNodes();
        int[] costs = nodes.stream().mapToInt(Node::getCost).toArray();
        GenericSolver ilsSolver = new IteratedLocalSearchSolver(
                distanceMatrix,
                objectiveMatrix,
                costs,
                nodes,
                15.9396f
        );
        Solution solution = ilsSolver.getSolution(0);
    }

}


