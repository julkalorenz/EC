package main.java.solver;
import main.java.models.Node;
import main.java.models.Solution;
import java.util.List;
import java.util.Objects;
import java.util.Set;


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
        for (int i = 0; i < path.length; i++) {
            if (path[i] == oldNodeID) {
                oldNodePosition = i;
                break;
            }
        }
        // get predecessor and successor of oldNode in the cycle
        int predecessorID = path[oldNodePosition - 1];
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
        for (int i = 0; i < path.length; i++) {
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

        // get predecessors and successors
        int pred1 = path[pos1 - 1];
        int succ1 = path[pos1 + 1];
        int pred2 = path[pos2 - 1];
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



    @Override
    public Solution getSolution(int startNodeID) {
        // 1. generate initial solution of size ceil(n/2) - random or greedy
        Solution currentSolution = getStartSolution(startNodeID);

        // 2. perform LS checking each neighbor, until no better solution is found in the neighborhood




        return null;
    }

}
