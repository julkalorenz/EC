package main.java.solver;
import java.util.*;

import main.java.models.Move;
import main.java.models.Node;
import main.java.models.Solution;
import main.java.utils.CSVParser;


public class LocalSearchCandidateMovesSolver extends LocalSearchSolver{

    private int candidateNeighborsCount;
    private Map<Integer, int[]> nearestNeighborsCache = new HashMap<>();
    // Steepest LS
    // Edge Exchange Intra-Route Neighborhood
    // Random Initial Solution
    public LocalSearchCandidateMovesSolver(int[][] distanceMatrix,
                                           int[][] objectiveMatrix,
                                           int[] costs,
                                           List<Node> nodes,
                                           int candidateNeighborsCount) {
        super(distanceMatrix,
                objectiveMatrix,
                costs,
                nodes,
                "Steepest",
                "Edge",
                "Random"
        );
        this.candidateNeighborsCount = candidateNeighborsCount;
        setMethodName("LocalSearchCandidateMovesSolver");

        for (int i = 0; i < distanceMatrix.length; i++) {
            nearestNeighborsCache.put(i, findNearestNeighbors(i));
        }
    }

    public int[] findNearestNeighbors(int currID) {
        int[][] distanceMatrix = getDistanceMatrix();
        int[] costs = getCosts();
        int n = distanceMatrix.length;

        PriorityQueue<int[]> heap = new PriorityQueue<>(
                (a, b) -> Integer.compare(b[1], a[1])  // sort by distance descending
        );

        for (int i = 0; i < n; i++) {
            if (i == currID) continue;

            int cost = distanceMatrix[currID][i] + costs[i];

            if (heap.size() < candidateNeighborsCount) {
                heap.offer(new int[]{i, cost});
            } else if (cost < heap.peek()[1]) {
                heap.poll();
                heap.offer(new int[]{i, cost});
            }
        }

        int count = heap.size();
        int[] nearestNeighbors = new int[count];
        for (int i = 0; i < count; i++) {
            nearestNeighbors[i] = heap.poll()[0];
        }

        return nearestNeighbors;
    }


    @Override
    public List<Move> getNeighborhood(Solution currentSolution, Set<Integer> nonSelectedNodeIDs, Set<Integer> allNodeIDs) {
        // for each node A in cycle -> find its 10 NNs (nearest neighbors), edge between A and NN is candidate edge
        // for each NN from the 10 NNs
        //     if NN not in cycle -> generate moves:
        //         node-swap between predA and NN
        //         node-swap between succA and NN
        //     if NN in cycle -> generate moves:
        //         edge-exchange between edge(predA, A) and edge(predNN, NN)
        //         edge-exchange between edge(A, succA) and edge(NN, succNN)

        Set<Integer> selectedNodeIDs = new HashSet<>(allNodeIDs);
        selectedNodeIDs.removeAll(nonSelectedNodeIDs);
        int[] cycle = currentSolution.getPath();
        int n = cycle.length - 1; // exclude return to start node

        Map<Integer, Integer> positionMap = new HashMap<>();
        for (int i = 0; i < n; i++) {
            positionMap.put(cycle[i], i);
        }

        List<Move> neighborhood = new ArrayList<>();

        for (int nodeID: selectedNodeIDs) {
            int[] NNs = nearestNeighborsCache.get(nodeID);
            int nodePos = positionMap.get(nodeID);
            int predNodeID = cycle[(nodePos - 1 + n) % n];
            int succNodeID = cycle[(nodePos + 1) % n];

            for (int nnID: NNs) {
                // case 1: nn not in the cycle -> generate node-swap moves
                if (nonSelectedNodeIDs.contains(nnID)) {
                    Move move1 = new Move("Inter", "-", predNodeID, nnID);
                    neighborhood.add(move1);
                    Move move2 = new Move("Inter", "-", succNodeID, nnID);
                    neighborhood.add(move2);
                }
                // case 2: nn in the cycle -> generate edge-exchange moves
                else if (selectedNodeIDs.contains(nnID)) {
                    // find nn position in cycle
                    int nnPos = positionMap.get(nnID);
                    int predNNID = cycle[(nnPos - 1 + n) % n];
                    int succNNID = cycle[(nnPos + 1) % n];

                    // edges cannot be adjacent
                    if (predNNID != nodeID && succNNID != nodeID) {
                        Move move1 = new Move("Intra", "Edge", predNodeID, predNNID);
                        neighborhood.add(move1);
                        Move move2 = new Move("Intra", "Edge", nodeID, nnID);
                        neighborhood.add(move2);
                    }
                }
            }
        }
        return neighborhood;
    }

    public static void main(String[] args) {

        String dataset = "TSPA";
        CSVParser parser = new CSVParser("src/main/data/" + dataset + ".csv", ";");
        int[][] distanceMatrix = parser.getDistanceMatrix();
        int[][] objectiveMatrix = parser.getObjectiveMatrix();
        List<Node> nodes = parser.getNodes();
        int[] costs = nodes.stream().mapToInt(Node::getCost).toArray();

        GenericSolver solver = new LocalSearchCandidateMovesSolver(
                distanceMatrix,
                objectiveMatrix,
                costs,
                nodes,
                10
        );
        System.out.println(solver.getMethodName());
        Solution solution = solver.getSolution(0);
        System.out.println("Solution Score: " + solution.getScore());
    }
}

