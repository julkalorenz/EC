package main.java.solver;

import main.java.models.*;

import java.util.*;
import java.util.stream.Collectors;

public class LocalSearchDeltaSolver extends LocalSearchSolver{
    private List<DeltaMove> moveList;
    private Set<String> moveSignatures;

    private Set<Edge> solutionEdges;
    private Set<Integer> solutionNodes;
    private int currentIteration;

    /// Maps a node identifier to its current index within the Hamiltonian cycle.
    private Map<Integer, Integer> nodePositions;

    // Number of iterations after which the entire neighborhood cache is rebuilt.
    private static final int REBUILD_INTERVAL = 25;


    public LocalSearchDeltaSolver(int[][] distanceMatrix, int[][] objectiveMatrix, int[] costs, List<Node> nodes) {
        super(distanceMatrix,
                objectiveMatrix,
                costs,
                nodes,
                "Steepest",
                "Edge",
                "Random");

        setMethodName("LocalSearchDeltaSolver");

        this.moveList = new ArrayList<>();
        this.solutionEdges = new HashSet<>();
        this.solutionNodes = new HashSet<>();
        this.moveSignatures = new HashSet<>();
        this.currentIteration = 0;
        this.nodePositions = new HashMap<>();
    }

    private DeltaMove Move2DeltaMove(Move move, Solution currentSolution){
        if (move.getType().equals("Intra") &&
                move.getIntraType().equals("Edge")){
            int[] path = currentSolution.getPath();
            int posA = nodePositions.get(move.getStartNodeID());
            int posB = nodePositions.get(move.getEndNodeID());

            int startNodeA = path[posA];
            int endNodeA = path[(posA + 1) % path.length];

            int startNodeB = path[posB];
            int endNodeB = path[(posB + 1) % path.length];

            Edge oldEdge1 = new Edge(startNodeA, endNodeA);
            Edge oldEdge2 = new Edge(startNodeB, endNodeB);

            int pos1 = Math.min(posA, posB);
            int pos2 = Math.max(posA, posB);

            int n = path.length - 1;
            int predA = (posA == 0) ? path[n - 1] : path[posA - 1];
            int succB = path[(posB + 1) % n];


            return new DeltaMove(
                    move.getType(),
                    move.getIntraType(),
                    startNodeA,
                    startNodeB,
                    move.getDelta(),
                    oldEdge1,
                    oldEdge2,
                    pos1,
                    pos2,
                    predA,
                    succB
            );

        } else if (move.getType().equals("Inter")) {
            int[] path = currentSolution.getPath();

            int oldNodeID = move.getStartNodeID();
            int newNodeID = move.getEndNodeID();

            int oldNodePos = nodePositions.get(oldNodeID);

            int n = path.length -1;
            int predecessorID = (oldNodePos == 0)
                    ? path[n - 1]
                    : path[oldNodePos - 1];
            int successorID = path[(oldNodePos + 1) % n];

            Edge oldEdge1 = new Edge(oldNodeID, successorID);
            Edge oldEdge2 = new Edge(predecessorID, oldNodeID);

            return new DeltaMove(
                    move.getType(),
                    move.getIntraType(),
                    oldNodeID,
                    newNodeID,
                    move.getDelta(),
                    oldEdge1,
                    oldEdge2,
                    oldNodePos,
                    -1,  // pos2 not used for inter moves
                    predecessorID,
                    successorID
            );
        }
        return null;
    }

    /**
     * Rebuilds the complete neighborhood cache from scratch. The method is
     * intentionally expensive and therefore executed only during the initial
     * setup and at fixed intervals to eliminate drift caused by incremental
     * updates.
     *
     * @param currentSolution solution that serves as reference for delta calculations
     * @param nonSelectedNodes nodes that are currently outside of the cycle
     * @param allNodes every node identifier in the instance
     */
    private void rebuildNeighborhood(Solution currentSolution,
                                     Set<Integer> nonSelectedNodes,
                                     Set<Integer> allNodes) {
        moveList.clear();
        moveSignatures.clear();

        List<Move> allMoves = getNeighborhood(currentSolution, nonSelectedNodes, allNodes);
        for (Move move : allMoves) {
            DeltaMove deltaMove = Move2DeltaMove(move, currentSolution);
            if (deltaMove != null && deltaMove.getDelta() < 0){
                String moveSignature = deltaMove.getSignature();
                if (!moveSignatures.contains(moveSignature)) {
                    moveList.add(deltaMove);
                    moveSignatures.add(moveSignature);
                }
            }
        }

        moveList.sort(Comparator.comparingInt(DeltaMove::getDelta));
    }


    /**
     * Validates whether a cached move is still applicable after a number of
     * incremental updates by inspecting structural invariants such as node
     * positions, incident edges and membership in the selected set.
     *
     * @return status describing whether the move can be applied, skipped,
     *         needs recalculation or should be dropped entirely
     */
    private MoveValidityStatus checkMoveValidity(DeltaMove move, Solution currentSolution, Set<Integer> nonSelectedNodes) {
        int[] path = currentSolution.getPath();

        if (move.getType().equals("Inter")) {
            int oldNodeID = move.getStartNodeID();
            int newNodeID = move.getEndNodeID();
            int pos = move.getPos1();

            // Check if replacement node is still outside
            if (!nonSelectedNodes.contains(newNodeID)) {
                return MoveValidityStatus.INVALID_REMOVE;
            }

            // Check if position still contains the original old node
            if (pos >= path.length - 1 || path[pos] != oldNodeID) {
                return MoveValidityStatus.INVALID_REMOVE;
            }

            // Check predecessor and successor relationships
            int n = path.length - 1;
            int currentPred = (pos == 0) ? path[n - 1] : path[pos - 1];
            int currentSucc = path[(pos + 1) % n];

            if (currentPred != move.getPredecessor() || currentSucc != move.getSuccessor()) {
                return MoveValidityStatus.RECALCULATE;
            }

            return MoveValidityStatus.VALID_APPLY;
        }

        else if (move.getType().equals("Intra") && move.getIntraType().equals("Edge")) {
            int pos1 = move.getPos1();
            int pos2 = move.getPos2();

            // Check if positions are valid
            if (pos1 >= path.length - 1 || pos2 >= path.length - 1) {
                return MoveValidityStatus.INVALID_REMOVE;
            }

            // Check if endpoints still contain the same nodes
            int nodeA = move.getStartNodeID();
            int nodeB = move.getEndNodeID();

            if (path[pos1] != nodeA && path[pos1] != nodeB) {
                return MoveValidityStatus.INVALID_REMOVE;
            }
            if (path[pos2] != nodeA && path[pos2] != nodeB) {
                return MoveValidityStatus.INVALID_REMOVE;
            }

            // Verify the edges still exist
            Edge oldEdge1 = move.getOldEdge1();
            Edge oldEdge2 = move.getOldEdge2();

            boolean edge1Exists = solutionEdges.contains(oldEdge1) ||
                    solutionEdges.contains(oldEdge1.reverse());
            boolean edge2Exists = solutionEdges.contains(oldEdge2) ||
                    solutionEdges.contains(oldEdge2.reverse());

            if (!edge1Exists || !edge2Exists) {
                return MoveValidityStatus.INVALID_REMOVE;
            }

            // Check if edges are in the same relative direction
            boolean edge1SameDirection = solutionEdges.contains(oldEdge1);
            boolean edge2SameDirection = solutionEdges.contains(oldEdge2);

            if (edge1SameDirection == edge2SameDirection) {
                return MoveValidityStatus.VALID_APPLY;
            } else {
                return MoveValidityStatus.VALID_SKIP;
            }
        }

        return MoveValidityStatus.INVALID_REMOVE;
    }

    /**
     * Computes the set of path indices that are influenced by a given intra
     * move. The result considers the endpoints, their immediate
     * neighbors, and every vertex contained inside the reversed segment so that
     * subsequent delta calculations can focus on the impacted area.
     *
     * @param move intra edge move whose footprint should be tracked
     * @param pathBeforeMove Hamiltonian cycle before applying the move
     * @return indices inside pathBeforeMove that must be refreshed
     */
    private Set<Integer> getAffectedPositionsIntra(DeltaMove move, int[] pathBeforeMove) {
        Set<Integer> affectedPos = new HashSet<>();

        int pos1 = move.getPos1();
        int pos2 = move.getPos2();
        int n = pathBeforeMove.length - 1; // Exclude duplicate end node

        // Add the edge endpoints
        affectedPos.add(pos1);
        affectedPos.add(pos2);

        // Add neighbors of endpoints (with wrap-around)
        affectedPos.add((pos1 - 1 + n) % n);
        affectedPos.add((pos1 + 1) % n);
        affectedPos.add((pos2 - 1 + n) % n);
        affectedPos.add((pos2 + 1) % n);

        // include ALL positions in the reversed segment
        for (int i = pos1 + 1; i <= pos2; i++) {
            affectedPos.add(i);
        }

        return affectedPos;
    }

    /**
     * Computes the set of path indices influenced by an inter move .
     * Only the position itself and its immediate neighbors are affected
     *
     * @param move inter move under evaluation
     * @param pathBeforeMove Hamiltonian cycle before applying the move
     * @return indices inside pathBeforeMove that must be refreshed
     */
    private Set<Integer> getAffectedPositionsInter(DeltaMove move, int[] pathBeforeMove) {
        Set<Integer> affectedPos = new HashSet<>();

        int pos = move.getPos1();
        int n = pathBeforeMove.length - 1;

        affectedPos.add(pos);
        affectedPos.add((pos - 1 + n) % n);
        affectedPos.add((pos + 1) % n);

        return affectedPos;
    }

    /**
     * Converts the affected positions of a move into explicit node identifiers
     *
     * @return node identifiers impacted by the move
     */
    private Set<Integer> getAffectedNodes(DeltaMove move, int[] pathBeforeMove) {
        Set<Integer> affected = new HashSet<>();
        Set<Integer> positions;

        if (move.getType().equals("Inter")) {
            positions = getAffectedPositionsInter(move, pathBeforeMove);
        } else if (move.getType().equals("Intra") && move.getIntraType().equals("Edge")) {
            positions = getAffectedPositionsIntra(move, pathBeforeMove);
        } else {
            return affected;
        }

        // Convert positions to node IDs
        for (int pos : positions) {
            if (pos < pathBeforeMove.length - 1) {
                affected.add(pathBeforeMove[pos]);
            }
        }

        if (move.getType().equals("Inter")) {
            affected.add(move.getEndNodeID());
        }

        return affected;
    }

    /**
     * Regenerates the subset of moves whose evaluation becomes stale after
     * applying the last improving move.
     */
    private void generateNewMoves(Solution currentSolution,
                                  Set<Integer> affectedNodes,
                                  Set<Integer> nonSelectedNodes,
                                  Set<Integer> allNodes,
                                  boolean isInterMove){
        Set<Integer> selectedNodeIDs = solutionNodes;

        int[] currentPath = currentSolution.getPath();
        int n = currentPath.length - 1;

        // For Inter moves: only regenerate inter moves for affected positions
        // The affected nodes are those whose edges changed (position and neighbors)
        if (isInterMove) {
            // Remove only inter moves involving affected nodes
            moveList.removeIf(m -> {
                if (m.getType().equals("Inter")) {
                    if (affectedNodes.contains(m.getStartNodeID())) {
                        moveSignatures.remove(m.getSignature());
                        return true;
                    }
                }
                return false;
            });

            // Regenerate inter moves ONLY for affected nodes
            for (int affectedNode : affectedNodes) {
                if (!selectedNodeIDs.contains(affectedNode)) continue;

                int pos = nodePositions.get(affectedNode);

                for (int outsideNode : nonSelectedNodes) {
                    int predPos = (pos - 1 + n) % n;
                    int succPos = (pos + 1) % n;

                    int predecessorID = currentPath[predPos];
                    int successorID = currentPath[succPos];

                    Edge oldEdge1 = new Edge(affectedNode, successorID);
                    Edge oldEdge2 = new Edge(predecessorID, affectedNode);

                    int delta = deltaNodeSwap(affectedNode, outsideNode, currentSolution);

                    if (delta < 0) {
                        DeltaMove deltaMove = new DeltaMove(
                                "Inter",
                                "-",
                                affectedNode,
                                outsideNode,
                                delta,
                                oldEdge1,
                                oldEdge2,
                                pos,
                                -1,
                                predecessorID,
                            successorID
                        );

                        String signature = deltaMove.getSignature();
                        if (!moveSignatures.contains(signature)) {
                            moveList.add(deltaMove);
                            moveSignatures.add(signature);
                        }
                    }
                }
            }
        }

        // Generate/regenerate Intra edge exchange moves for affected nodes
        // Remove existing intra moves involving affected nodes
        moveList.removeIf(m -> {
            if (m.getType().equals("Intra") && m.getIntraType().equals("Edge")) {
                if (affectedNodes.contains(m.getStartNodeID()) ||
                        affectedNodes.contains(m.getEndNodeID())) {
                    moveSignatures.remove(m.getSignature());
                    return true;
                }
            }
            return false;
        });

        // Regenerate intra moves for affected nodes
        for (int nodeA : affectedNodes) {
            if (!selectedNodeIDs.contains(nodeA)) continue;

            int posA = nodePositions.get(nodeA);

            // Generate moves with all other selected nodes
            for (int posB = 0; posB < n; posB++) {
                int nodeB = currentPath[posB];

                if (nodeA == nodeB) continue;
                if (!selectedNodeIDs.contains(nodeB)) continue;

                // Ensure edges are not adjacent
                if (Math.abs(posA - posB) <= 1) continue;
                if (posA == 0 && posB == n - 1) continue;
                if (posB == 0 && posA == n - 1) continue;

                int pos1 = Math.min(posA, posB);
                int pos2 = Math.max(posA, posB);

                int node1 = currentPath[pos1];
                int node2 = currentPath[pos2];

                Edge oldEdge1 = new Edge(node1, currentPath[(pos1 + 1) % n]);
                Edge oldEdge2 = new Edge(node2, currentPath[(pos2 + 1) % n]);

                int predPos1 = (pos1 - 1 + n) % n;
                int succPos2 = (pos2 + 1) % n;
                int pred1 = currentPath[predPos1];
                int succ2 = currentPath[succPos2];

                int delta = deltaEdgeExchange(node1, node2, currentSolution);

                if (delta < 0) {
                    DeltaMove deltaMove = new DeltaMove(
                            "Intra",
                            "Edge",
                            node1,
                            node2,
                            delta,
                            oldEdge1,
                            oldEdge2,
                            pos1,
                            pos2,
                            pred1,
                            succ2
                    );

                    String signature = deltaMove.getSignature();
                    if (!moveSignatures.contains(signature)) {
                        moveList.add(deltaMove);
                        moveSignatures.add(signature);
                    }
                }
            }
        }
    }

    /**
     * Recomputes structures describing the Solution (node positions and edge set) from the
     * supplied path.

     */
    private void rebuildStructures(int[] path) {
        // Rebuild node positions
        nodePositions.clear();
        for (int i = 0; i < path.length - 1; i++) {
            nodePositions.put(path[i], i);
        }

        // Rebuild solution edges
        solutionEdges.clear();
        for (int i = 0; i < path.length; i++) {
            int startNodeID = path[i];
            int endNodeID = path[(i + 1) % path.length];
            solutionEdges.add(new Edge(startNodeID, endNodeID));
        }
    }

    /**
     * Repeatedly picks the best improving move,
     * applies it, and regenerates only the affected parts of the neighborhood
     * until no improvements remain.
     */
    @Override
    public Solution steepestLocalSearch(Solution currentSolution, Set<Integer> allNodeIDs) {
        solutionNodes = Arrays.stream(currentSolution.getPath())
                .boxed().collect(Collectors.toSet());
        Set<Integer> nonSelectedNodeIDs = new HashSet<>(allNodeIDs);
        nonSelectedNodeIDs.removeAll(solutionNodes);

        int[] path = currentSolution.getPath();

        // Initialize structures
        rebuildStructures(path);

        rebuildNeighborhood(currentSolution, nonSelectedNodeIDs, allNodeIDs);

        currentIteration = 0;

        while (!moveList.isEmpty()) {
            // Periodic full rebuild
            if (currentIteration > 0 && currentIteration % REBUILD_INTERVAL == 0) {
                rebuildNeighborhood(currentSolution, nonSelectedNodeIDs, allNodeIDs);
            }

            boolean moveApplied = false;

            // Iterate through list from best to worst
            for (int i = 0; i < moveList.size(); i++) {
                DeltaMove move = moveList.get(i);

                MoveValidityStatus status = checkMoveValidity(move, currentSolution, nonSelectedNodeIDs);

                if (status == MoveValidityStatus.INVALID_REMOVE) {
                    moveList.remove(i);
                    moveSignatures.remove(move.getSignature());
                    i--; // Adjust index after removal
                    continue;
                }

                if (status == MoveValidityStatus.VALID_SKIP) {
                    continue;
                }

                if (status == MoveValidityStatus.RECALCULATE) {
                    int newDelta;
                    if (move.getType().equals("Inter")) {
                        newDelta = deltaNodeSwap(move.getStartNodeID(), move.getEndNodeID(), currentSolution);
                    } else {
                        newDelta = deltaEdgeExchange(move.getStartNodeID(), move.getEndNodeID(), currentSolution);
                    }

                    // Create updated move with current solution state
                    Move tempMove = new Move(
                            move.getType(),
                            move.getIntraType(),
                            move.getStartNodeID(),
                            move.getEndNodeID(),
                            newDelta
                    );
                    DeltaMove updatedMove = Move2DeltaMove(tempMove, currentSolution);

                    // Replace old move with updated one
                    moveList.remove(i);
                    moveSignatures.remove(move.getSignature());

                    if (updatedMove != null && updatedMove.getDelta() < 0) {
                        String sig = updatedMove.getSignature();
                        if (!moveSignatures.contains(sig)) {
                            moveList.add(updatedMove);
                            moveSignatures.add(sig);
                        }
                    }
                    i--;
                    continue;
                }

                // status == VALID_APPLY
                if (move.getDelta() < 0) {
                    Set<Integer> affectedNodes = getAffectedNodes(move, currentSolution.getPath());
                    boolean isInterMove = move.getType().equals("Inter");

                    currentSolution = applyMove(currentSolution, move);

                    // Update selected/non-selected sets for inter moves
                    if (isInterMove) {
                        nonSelectedNodeIDs.add(move.getStartNodeID());
                        nonSelectedNodeIDs.remove(move.getEndNodeID());
                        solutionNodes.remove(move.getStartNodeID());
                        solutionNodes.add(move.getEndNodeID());
                    }
                    rebuildStructures(currentSolution.getPath());

                    currentIteration++;
                    currentSolution.setIterationCount(currentIteration);

                    // Remove the applied move
                    moveList.remove(i);
                    moveSignatures.remove(move.getSignature());

                    // Generate new moves - pass isInterMove flag
                    generateNewMoves(currentSolution, affectedNodes, nonSelectedNodeIDs, allNodeIDs, isInterMove);

                    // Re-sort the list to maintain best-first order
                    moveList.sort(Comparator.comparingInt(DeltaMove::getDelta));

                    moveApplied = true;
                    break; // Start from beginning of list again
                } else {
                    // Move delta is no longer improving, remove it
                    moveList.remove(i);
                    moveSignatures.remove(move.getSignature());
                    i--;
                }
            }

            if (!moveApplied) {
                // No improving move found after checking entire list
                break;
            }
        }

        return currentSolution;
    }


    @Override
    public Solution getSolution(int startNodeID) {
        Set<Integer> allNodeIDs = getNodes().stream()
                .map(Node::getId)
                .collect(Collectors.toSet());

        Solution currentSolution = getStartSolution(startNodeID);
        currentSolution.setIterationCount(0);


        return steepestLocalSearch(currentSolution, allNodeIDs);
    }

}
