package main.java.solver;

import main.java.models.Node;
import main.java.models.Solution;

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

    public int[] destroy(Solution solution) {
        // remove 20-40% of the nodes from the solution (default 30%)
        // select a few subpaths, select randomly, but heuristically remove bad edges, but not fully deterministic (long edges or costly nodes)
        // and remove them from the solution,
        // return a new smaller cycle

        int[] fullPath = solution.getPath();
        int nodesToRemove = (int) (REMOVE_FRACTION * (fullPath.length - 1));

        // split path into 5-node segments
        // evaluate edges in each segment, and node costs
        // remove nodesToRemove/5 segments with probability based on worst score
        // connect rest of the segments

        return new int[0];
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
