package main.java.solver;
import main.java.models.Move;
import main.java.models.Node;
import main.java.models.Solution;
import main.java.utils.CSVParser;

import java.util.*;
import java.util.stream.Collectors;


public class LocalSearchSolver extends GenericSolver {

    private final String localSearchType; // "Greedy" or "Steepest"
    private final String neighborhoodType; // "Node" or "Edge"
    private final String startSolutionType; // "Random" or "Greedy"

    public LocalSearchSolver(
            int[][] distanceMatrix,
            int[][] objectiveMatrix,
            int[] costs,
            List<Node> nodes,
            String localSearchType,
            String neighborhoodType,
            String startSolutionType
    ) {
        // "Greedy_LS-Node_Exchange-Greedy_Start"
        // "Steepest_LS-Edge_Exchange-Random_Start"
        super(distanceMatrix, objectiveMatrix, costs, nodes, (localSearchType + "_LS-" + neighborhoodType + "_Exchange-" + startSolutionType + "_Start"));
        this.localSearchType = localSearchType;
        this.neighborhoodType = neighborhoodType;
        this.startSolutionType = startSolutionType;
    }

    public Solution getStartSolution(int startNodeID) {
        if (Objects.equals(startSolutionType, "Random")) {
            GenericSolver randomSolver = new RandomSolver(
                    getDistanceMatrix(),
                    getObjectiveMatrix(),
                    getCosts(),
                    getNodes()
            );
            return randomSolver.getSolution(startNodeID);
        } else if (Objects.equals(startSolutionType, "Greedy")) {
            GenericSolver greedySolver = new NNAny2RegretWeightedSolver(
                    getDistanceMatrix(),
                    getObjectiveMatrix(),
                    getCosts(),
                    getNodes());
            return greedySolver.getSolution(startNodeID);
        }
        return null;
    }

    /**
     * Delta evaluation for inter-route move: swap of two nodes (one from the cycle, one from outside)
     * @param oldNodeID
     * @param newNodeID
     * @param currentSolution
     * @return change in cost (delta): positive if worse, negative if better
     */
    public int deltaNodeSwap(int oldNodeID, int newNodeID, Solution currentSolution) {
        // find positions of oldNode
        int[] path = currentSolution.getPath();
        int oldNodePosition = -1;
        for (int i = 0; i < path.length - 1; i++) {
            if (path[i] == oldNodeID) {
                oldNodePosition = i;
                break;
            }
        }
        // get predecessor and successor of oldNode in the cycle
        int predecessorID = (oldNodePosition == 0)
                ? path[path.length - 2]   // predecessor of first node is the last real node
                : path[oldNodePosition - 1];
        int successorID = path[oldNodePosition + 1];

        // compute delta
        int oldCost = getDistanceMatrix()[predecessorID][oldNodeID] + getCosts()[oldNodeID] + getDistanceMatrix()[oldNodeID][successorID];
        int newCost = getDistanceMatrix()[predecessorID][newNodeID] + getCosts()[newNodeID] + getDistanceMatrix()[newNodeID][successorID];

        return newCost - oldCost; // positive if worse, negative if better
    }

    /**
     * Delta evaluation for intra-route move:
     * 2 node exchange within the cycle
     * @param nodeID1
     * @param nodeID2
     * @param currentSolution
     * @return change in cost (delta): positive if worse, negative if better
     */
    public int deltaNodeExchange(int nodeID1, int nodeID2, Solution currentSolution) {
        // two nodes: nodeID1, nodeID2 -> swap their positions in the cycle

        // find positions of nodeID1 and nodeID2
        int[] path = currentSolution.getPath();
        int pos1 = -1;
        int pos2 = -1;
        for (int i = 0; i < path.length - 1; i++) {
            if (path[i] == nodeID1) {
                pos1 = i;
                if (pos2 != -1) {
                    break;
                }
            } else if (path[i] == nodeID2) {
                pos2 = i;
                if (pos1 != -1) {
                    break;
                }
            }
        }
        if (Math.abs(pos1 - pos2) == 1 ||
                (pos1 == 0 && pos2 == path.length - 2) ||
                (pos2 == 0 && pos1 == path.length - 2)) {
            // For adjacent nodes, swapping only changes the direction of travel
            // Since distance matrix is symmetric, delta is 0
            return 0;
        }

        // get predecessors and successors
        // Cyclic predecessor and successor for pos1
        int pred1 = (pos1 == 0) ? path[path.length - 2] : path[pos1 - 1];
        int succ1 = path[pos1 + 1];
        // Cyclic predecessor and successor for pos2
        int pred2 = (pos2 == 0) ? path[path.length - 2] : path[pos2 - 1];
        int succ2 = path[pos2 + 1];

        // compute delta
        int oldCost = getDistanceMatrix()[pred1][nodeID1] + getDistanceMatrix()[nodeID1][succ1]
                + getDistanceMatrix()[pred2][nodeID2] + getDistanceMatrix()[nodeID2][succ2];
        int newCost = getDistanceMatrix()[pred1][nodeID2] + getDistanceMatrix()[nodeID2][succ1]
                + getDistanceMatrix()[pred2][nodeID1] + getDistanceMatrix()[nodeID1][succ2];

        return newCost - oldCost; // positive if worse, negative if better
    }

    /**
     * Delta evaluation for intra-route move:
     * 2 edge exchange within the cycle
     * @param startNodeID1
     * @param startNodeID2
     * @param currentSolution
     * @return change in cost (delta): positive if worse, negative if better
     */
    public int deltaEdgeExchange(int startNodeID1, int startNodeID2, Solution currentSolution) {
        // two edges: (startNodeID1, endNodeID1), (startNodeID2, endNodeID2) ->
        // -> (startNodeID1, startNodeID2), (endNodeID1, endNodeID2) and nodes in between are reversed
        // find positions of startNodeID1 and startNodeID2
        int[] path = currentSolution.getPath();
        int pos1 = -1;
        int pos2 = -1;
        for (int i = 0; i < path.length; i++) {
            if (path[i] == startNodeID1) {
                pos1 = i;
                if (pos2 != -1) {
                    break;
                }
            } else if (path[i] == startNodeID2) {
                pos2 = i;
                if (pos1 != -1) {
                    break;
                }
            }
        }

        //  get end nodes
        int endNodeID1 = path[pos1 + 1];
        int endNodeID2 = path[pos2 + 1];

        // compute delta
        int oldCost = getDistanceMatrix()[startNodeID1][endNodeID1] + getDistanceMatrix()[startNodeID2][endNodeID2];
        int newCost = getDistanceMatrix()[startNodeID1][startNodeID2] + getDistanceMatrix()[endNodeID1][endNodeID2];
        return newCost - oldCost; // positive if worse, negative if better
    }


    /**
    * Generate neighborhood moves based on the current solution and neighborhood type
    * @param currentSolution The current solution
    * @param nonSelectedNodeIDs Set of node IDs not included in the current solution
    * @param allNodeIDs Set of all node IDs in the problem
    * @return List of possible moves in the neighborhood
     */
    public List<Move> getNeighborhood(Solution currentSolution, Set<Integer> nonSelectedNodeIDs, Set<Integer> allNodeIDs) {

        // depending on neighborhoodType, generate list of possible moves
        // 1. generate all inter moves (node swaps)
        Set<Integer> selectedNodeIDs = new HashSet<>(allNodeIDs);
        selectedNodeIDs.removeAll(nonSelectedNodeIDs);
        List<Move> allMoves = new ArrayList<>();
        int[] cycle = currentSolution.getPath();

        for (int inNodeID: selectedNodeIDs) {
            for (int outNodeID: nonSelectedNodeIDs) {
                Move move = new Move("Inter", "-", inNodeID, outNodeID);
                allMoves.add(move);
            }
        }
        // 2. generate all intra moves (node exchanges OR edge exchanges) depending on neighborhoodType
        if (Objects.equals(neighborhoodType, "Node")) {
            for (int i = 0; i < cycle.length - 1; i++) {
                for (int j = i + 1; j < cycle.length - 1; j++) {
                    Move move = new Move("Intra", "Node", cycle[i], cycle[j]);
                    allMoves.add(move);
                }
            }
        }
        else if (Objects.equals(neighborhoodType, "Edge")) {
            for (int i = 0; i < cycle.length - 1; i++) {
                for (int j = i + 2; j < cycle.length - 1; j++) { // ensure edges are not adjacent
                    if (i == 0 && j == cycle.length - 2) {
                        continue; // skip if first and last edge (they are adjacent in a cycle)
                    }
                    Move move = new Move("Intra", "Edge", cycle[i], cycle[j]);
                    allMoves.add(move);
                }
            }
        }
        return allMoves;
    }

    public Solution applyMove(Solution currentSolution, Move move) {
        int[] cycle = currentSolution.getPath();
        int[] newCycle = Arrays.copyOf(cycle, cycle.length);

        // 1. Apply inter move: node swap - replace oldNodeID with newNodeID in the cycle
        if (Objects.equals(move.getType(), "Inter")) {
            for (int i = 0; i < newCycle.length; i++) {
                if (newCycle[i] == move.getStartNodeID()) {
                    newCycle[i] = move.getEndNodeID();
                    // (start == end) update end node too
                    if (i == 0 && newCycle[newCycle.length - 1] == move.getStartNodeID()) {
                        newCycle[newCycle.length - 1] = move.getEndNodeID();
                    } else if (i == newCycle.length - 1 && newCycle[0] == move.getStartNodeID()) {
                        newCycle[0] = move.getEndNodeID();
                    }
                    break;
                }
            }
        } else if (Objects.equals(move.getType(), "Intra")) {
            if (Objects.equals(move.getIntraType(), "Node")) {
                // if intra node exchange - swap positions of the two nodes in the cycle
                int pos1 = -1;
                int pos2 = -1;
                for (int i = 0; i < newCycle.length; i++) {
                    if (newCycle[i] == move.getStartNodeID()) pos1 = i;
                    else if (newCycle[i] == move.getEndNodeID()) pos2 = i;
                    if (pos1 != -1 && pos2 != -1) break;
                }
                // swap
                int temp = newCycle[pos1];
                newCycle[pos1] = newCycle[pos2];
                newCycle[pos2] = temp;
            } else if (Objects.equals(move.getIntraType(), "Edge")) {
                // if intra edge exchange - reverse the segment between the two edges
                int pos1 = -1;
                int pos2 = -1;
                for (int i = 0; i < newCycle.length; i++) {
                    if (newCycle[i] == move.getStartNodeID()) pos1 = i;
                    else if (newCycle[i] == move.getEndNodeID()) pos2 = i;
                    if (pos1 != -1 && pos2 != -1) break;
                }
                if (pos1 > pos2) {
                    int temp = pos1;
                    pos1 = pos2;
                    pos2 = temp;
                }
                // reverse segment between pos1+1 and pos2
                int left = pos1 + 1;
                int right = pos2;
                while (left < right) {
                    int temp = newCycle[left];
                    newCycle[left] = newCycle[right];
                    newCycle[right] = temp;
                    left++;
                    right--;
                }
            }
        }
        return new Solution(
                getNodes(),
                getObjectiveMatrix(),
                getDistanceMatrix(),
                getCosts(),
                newCycle,
                this.getMethodName()
        );
    }

    public Solution greedyLocalSearch(Solution currentSolution, Set<Integer> allNodeIDs) {
        Set<Integer> selectedNodeIDs = Arrays.stream(currentSolution.getPath()).boxed().collect(Collectors.toSet());
        Set<Integer> nonSelectedNodeIDs = new HashSet<>(allNodeIDs);
        nonSelectedNodeIDs.removeAll(selectedNodeIDs);

        int iteration = 0;
        while (true) {
            List<Move> neighborhood = getNeighborhood(currentSolution, nonSelectedNodeIDs, allNodeIDs);
            // shuffle the list of moves
            Collections.shuffle(neighborhood);
            boolean improved = false;
            for (Move move: neighborhood) {
                // evaluate move
                int delta;
                if (Objects.equals(move.getType(), "Inter")) {
                    delta = deltaNodeSwap(move.getStartNodeID(), move.getEndNodeID(), currentSolution);
                } else if (Objects.equals(move.getType(), "Intra")) {
                    if (Objects.equals(move.getIntraType(), "Node")) {
                        delta = deltaNodeExchange(move.getStartNodeID(), move.getEndNodeID(), currentSolution);
                    } else if (Objects.equals(move.getIntraType(), "Edge")) {
                        delta = deltaEdgeExchange(move.getStartNodeID(), move.getEndNodeID(), currentSolution);
                    } else {
                        continue; // unknown intra type
                    }
                } else {
                    continue; // unknown move type
                }
                if (delta < 0) { // found first improving move
                    currentSolution = applyMove(currentSolution, move);
                    // update selected and non-selected node IDs
                    if (Objects.equals(move.getType(), "Inter")) {
                        selectedNodeIDs.remove(move.getStartNodeID());
                        selectedNodeIDs.add(move.getEndNodeID());
                        nonSelectedNodeIDs.add(move.getStartNodeID());
                        nonSelectedNodeIDs.remove(move.getEndNodeID());
                    }
                    improved = true;
                    break;
                }
            }
            iteration++;
            if (!improved) {
                System.out.println("Local Search converged after " + iteration + " iterations.");
                return currentSolution;// no improving move found -> end Local Search
            }
        }
    }




    @Override
    public Solution getSolution(int startNodeID) {
        // 1. generate initial solution of size ceil(n/2) - random or greedy
        Set<Integer> allNodeIDs = getNodes().stream().map(Node::getId).collect(Collectors.toSet());
        Solution currentSolution = getStartSolution(startNodeID);
        // 2. perform LS checking each neighbor, until no better solution is found in the neighborhood
        if (Objects.equals(localSearchType, "Greedy")) {
            return greedyLocalSearch(currentSolution, allNodeIDs);
        }
        return null;
    }

    public static void main(String[] args) {
        // Example usage of LocalSearchSolver
        String type = "Greedy";
        String neighborhood = "Node";
        String start = "Random";
        String dataset = "TSPB";
        CSVParser parser = new CSVParser("src/main/data/" + dataset + ".csv", ";");
        int[][] distanceMatrix = parser.getDistanceMatrix();
        int[][] objectiveMatrix = parser.getObjectiveMatrix();
        List<Node> nodes = parser.getNodes();
        int[] costs = nodes.stream().mapToInt(Node::getCost).toArray();

        GenericSolver solver = new LocalSearchSolver(
                distanceMatrix,
                objectiveMatrix,
                costs,
                nodes,
                type,
                neighborhood,
                start
        );
        System.out.println(solver.getMethodName());
        Solution solution = solver.getSolution(0);
        System.out.println("Solution Score: " + solution.getScore());


    }

}
