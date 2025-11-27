package main.java.solver;

import main.java.models.Node;
import main.java.models.Solution;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LargeNeighborhoodSearchSolver extends GenericSolver {
    private float stoppingTimeSeconds;
    private double REMOVE_FRACTION = 0.3;
    private boolean USE_LOCAL_SEARCH_AFTER_REPAIR = true;

    public LargeNeighborhoodSearchSolver(
            int[][] distanceMatrix,
            int[][] objectiveMatrix,
            int[] costs,
            List<Node> nodes,
            float stoppingTimeSeconds
    ) {
        super(distanceMatrix, objectiveMatrix, costs, nodes, "Large Neighborhood Search");
        this.stoppingTimeSeconds = stoppingTimeSeconds;
    }

    public LargeNeighborhoodSearchSolver(
            int[][] distanceMatrix,
            int[][] objectiveMatrix,
            int[] costs,
            List<Node> nodes,
            float stoppingTimeSeconds,
            double removeFraction,
            boolean useLocalSearchAfterRepair
    ) {
        super(distanceMatrix, objectiveMatrix, costs, nodes, "Large Neighborhood Search");
        this.stoppingTimeSeconds = stoppingTimeSeconds;
        this.REMOVE_FRACTION = removeFraction;
        this.USE_LOCAL_SEARCH_AFTER_REPAIR = useLocalSearchAfterRepair;
    }
    private class Segment {
        int startIndex;
        int cost;

        public Segment(int startIndex, int cost) {
            this.startIndex = startIndex;
            this.cost = cost;
        }
    }

    public int[] destroy(Solution solution) {

        // In a nutshell: we will remove segments of 5 nodes from the solution path
        // The segments will be selected using a roulette wheel selection method
        // based on the cost of the segments (higher cost = higher chance of being selected)

        int[] fullPath = solution.getPath();
        int pathLength = fullPath.length - 1;
        int nodesToRemoveCount = (int) (REMOVE_FRACTION * pathLength);
        int segmentLength = 5;
        int segmentsNeeded = nodesToRemoveCount / segmentLength;

        List<Segment> availableSegments = new ArrayList<>();

        for (int i = 0; i < pathLength; i++) {
            int cost = 0;
            for (int k = 0; k < segmentLength; k++) {
                int idx = (i + k) % pathLength;
                int nextIdx = (i + k + 1) % pathLength;
                cost += getCosts()[fullPath[idx]] + getDistanceMatrix()[fullPath[idx]][fullPath[nextIdx]];
            }
            availableSegments.add(new Segment(i, cost));
        }

        // Prepare to track removed nodes to avoid overlaps
        boolean[] removedNodes = new boolean[pathLength];
        int segmentsRemoved = 0;

        // Roulette Wheel Selection Loop
        while (segmentsRemoved < segmentsNeeded && !availableSegments.isEmpty()) {

            // Calculate Total Cost of currently VALID segments
            int totalCost = 0;
            List<Segment> validSegments = new ArrayList<>();

            for (Segment seg : availableSegments) {
                // Check if this segment overlaps with already removed nodes
                boolean overlaps = false;
                for (int k = 0; k < segmentLength; k++) {
                    int idx = (seg.startIndex + k) % pathLength;
                    if (removedNodes[idx]) {
                        overlaps = true;
                        break;
                    }
                }

                if (!overlaps) {
                    validSegments.add(seg);
                    totalCost += seg.cost;
                }
            }

            if (validSegments.isEmpty()) break; // No more space to remove segments

            // Spin the Wheel
            int spin = (int) (Math.random() * totalCost);
            int currentSum = 0;
            Segment selectedSegment = null;

            for (Segment seg : validSegments) {
                currentSum += seg.cost;
                if (currentSum >= spin) {
                    selectedSegment = seg;
                    break;
                }
            }

            // Fallback
            if (selectedSegment == null) selectedSegment = validSegments.getLast();

            // Remove the selected segment
            for (int k = 0; k < segmentLength; k++) {
                int idx = (selectedSegment.startIndex + k) % pathLength;
                removedNodes[idx] = true;
            }
            segmentsRemoved++;

            availableSegments.remove(selectedSegment);
        }

        // Reconstruct Path
        int[] partialPath = new int[pathLength - (segmentsRemoved * segmentLength)];
        int currentIdx = 0;
        for (int i = 0; i < pathLength; i++) {
            if (!removedNodes[i]) {
                partialPath[currentIdx++] = fullPath[i];
            }
        }

        return partialPath;
    }

    public Solution repair(int[] partialPath) {
        // reinsert nodes in the best possible way
        // As repair operator use the best greedy heuristic (including greedy-regret) from previous assignments.
        // nn any wieghted 2 regret
        // return a complete solution

        NNAny2RegretWeightedSolver repairSolver = new NNAny2RegretWeightedSolver(
                getDistanceMatrix(),
                getObjectiveMatrix(),
                getCosts(),
                getNodes()
        );
        return repairSolver.completeSolution(partialPath);
    }

    @Override
    public Solution getSolution(int startNodeID) {
        // for LS - LocalSearchSolver "steepest", "edge", "random", function steepestLocalSearch(solution, allNodeIDs)
        // for repair - NNAny2RegretWeightedSolver, function completeSolution(int[] incompletePath)
        // for initial solution - RandomSolver, function getSolution(int startNodeID)

        // get initial solution - random - x
        // local search - x
        // while not stopping criteria
        //    destroy - y
        //    repair - y
        //    OPTIONAL: local search
        //    if y better than x, update

        // Step 1 - get random solution
        RandomSolver initialSolver = new RandomSolver(
                getDistanceMatrix(),
                getObjectiveMatrix(),
                getCosts(),
                getNodes()
        );
        Solution initialSolution = initialSolver.getSolution(startNodeID);

        // Step 2 - local search on initial solution
        LocalSearchSolver localSearchSolver = new LocalSearchSolver(
                getDistanceMatrix(),
                getObjectiveMatrix(),
                getCosts(),
                getNodes(),
                "steepest",
                "edge",
                "random"
        );
        Set<Integer> allNodeIDs = new HashSet<>();
        for (Node node : getNodes()) {
            allNodeIDs.add(node.getId());
        }
        Solution currentSolution = localSearchSolver.steepestLocalSearch(initialSolution, allNodeIDs);
        Solution bestSolution = currentSolution;
        float bestScore = bestSolution.getScore();

        long startTime = System.nanoTime();
        while (true) {
            long currentTime = System.nanoTime();
            float elapsedTimeSeconds = (currentTime - startTime) / 1_000_000_000.0f;
            if (elapsedTimeSeconds >= stoppingTimeSeconds) {
                break;
            }
            // destroy
            int[] partialPath = destroy(currentSolution);
            // repair
            Solution repairedSolution = repair(partialPath);
            // optional local search
            if (USE_LOCAL_SEARCH_AFTER_REPAIR) {
                repairedSolution = localSearchSolver.steepestLocalSearch(repairedSolution, allNodeIDs);
            }
            // update best
            if (repairedSolution.getScore() < bestScore) {
                bestSolution = repairedSolution;
                bestScore = bestSolution.getScore();
                currentSolution = repairedSolution;
            }
        }

        return null;
    }
}
