package main.java.solver;

import main.java.models.*;

import java.util.*;
import java.util.stream.Collectors;

public class LocalSearchDeltaSolver extends LocalSearchSolver{
    /**
     * Local Searchh Solver that uses delta evaluations from previous iterations to speed up the process
     * If a given delta is no longer valid (edges have changed), it will be recalculated
     */

    private PriorityQueue<DeltaMove> moveQueue;
    private Set<String> moveSignatures;
    private Set<DeltaMove> skippedMoves;
    private int currentIteration;

    public LocalSearchDeltaSolver(int[][] distanceMatrix, int[][] objectiveMatrix, int[] costs, List<Node> nodes) {
        super(distanceMatrix,
                objectiveMatrix,
                costs,
                nodes,
                "Steepest",
                "Edge",
                "Random");

        setMethodName("LocalSearchDeltaSolver");

        this.moveQueue = new PriorityQueue<>(Comparator.comparingInt(DeltaMove::getDelta));
        this.moveSignatures = new HashSet<>();
        this.skippedMoves = new HashSet<>();
        this.currentIteration = 0;
    }

    private int findNodePosition(int[] path, int nodeID){
        for (int i = 0; i < path.length; i++) {
            if (path[i] == nodeID) {
                return i;
            }
        }
        return -1;
    }

    private boolean edgeExists(int[] path, int start, int end){
        for (int i = 0; i < path.length - 1; i++) {
            if ((path[i] == start && path[i + 1] == end) ||
                    (path[i] == end && path[i + 1] == start)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkEdgeDirection(int[] path, int start, int end, boolean expectedForward) {
        for (int i = 0; i < path.length - 1; i++) {
            if (path[i] == start && path[i + 1] == end) {
                return expectedForward;
            }
            if (path[i] == end && path[i + 1] == start) {
                return !expectedForward;
            }
        }
        return false;
    }

    private DeltaMove Move2DeltaMove(Move move, Solution currentSolution){
        if (move.getType().equals("Intra") &&
            move.getIntraType().equals("Edge")){
            int[] path = currentSolution.getPath();
            int posA = findNodePosition(path, move.getStartNodeID());
            int posB = findNodePosition(path, move.getEndNodeID());

            int startNodeA = path[posA];
            int endNodeA = path[(posA + 1) % path.length];

            int startNodeB = path[posB];
            int endNodeB = path[(posB + 1) % path.length];

            boolean edge1Dir = true;
            boolean edge2Dir = true;

            return new DeltaMove(
                    move.getType(),
                    move.getIntraType(),
                    startNodeA,
                    startNodeB,
                    move.getDelta(),
                    endNodeA,
                    endNodeB,
                    edge1Dir,
                    edge2Dir
            );

        } else if (move.getType().equals("Inter")) {
            int[] path = currentSolution.getPath();

            int oldNodeID = move.getStartNodeID();
            int newNodeID = move.getEndNodeID();

            int oldNodePos = findNodePosition(path, oldNodeID);

            int n = path.length -1;
            int predecessorID = (oldNodePos == 0)
                    ? path[n - 1]
                    : path[oldNodePos - 1];
            int successorID = path[(oldNodePos + 1) % n];

            return new DeltaMove(
                    move.getType(),
                    move.getIntraType(),
                    oldNodeID,
                    newNodeID,
                    move.getDelta(),
                    successorID,
                    predecessorID,
                    true,
                    true
            );
        }
        return null;
    }

    private MoveValidityStatus validateInterMove(DeltaMove move,
                                                 Solution currentSolution,
                                                 Set<Integer> nonSelectedNodeIDs) {
        int oldNodeID = move.getStartNodeID();
        int newNodeID = move.getEndNodeID();

        if (!nonSelectedNodeIDs.contains(newNodeID)) {
            return MoveValidityStatus.INVALID_REMOVE;
        }

        int[] path = currentSolution.getPath();
        int oldNodePos = findNodePosition(path, oldNodeID);
        if (oldNodePos == -1) {
            return MoveValidityStatus.INVALID_REMOVE;
        }

        int n = path.length - 1;
        int currentPred = (oldNodePos == 0) ? path[n - 1] : path[oldNodePos - 1];
        int currentSucc = path[(oldNodePos + 1) % n];

        int storedSucc = move.getEdge1End();
        int storedPred = move.getEdge2End();

        if (currentPred != storedPred || currentSucc != storedSucc) {
            return MoveValidityStatus.RECALCULATE;
        }

        return MoveValidityStatus.VALID_APPLY;
    }

    private MoveValidityStatus validateIntraEdgeMove(DeltaMove move, Solution currentSolution) {
        int[] path = currentSolution.getPath();

        int edge1Start = move.getEdge1Start();
        int edge1End = move.getEdge1End();
        int edge2Start = move.getEdge2Start();
        int edge2End = move.getEdge2End();

        boolean edge1Exists = edgeExists(path, edge1Start, edge1End);
        boolean edge2Exists = edgeExists(path, edge2Start, edge2End);

        if (!edge1Exists || !edge2Exists) {
            return MoveValidityStatus.INVALID_REMOVE;
        }

        boolean edge1SameDir = checkEdgeDirection(path, edge1Start, edge1End,
                move.isEdge1Direction());
        boolean edge2SameDir = checkEdgeDirection(path, edge2Start, edge2End,
                move.isEdge2Direction());

        if ((edge1SameDir && edge2SameDir) || (!edge1SameDir && !edge2SameDir)) {
            return MoveValidityStatus.VALID_APPLY;
        }

        return MoveValidityStatus.VALID_SKIP;
    }

    private MoveValidityStatus checkMoveValidity(DeltaMove move,
                                                 Solution currentSolution,
                                                 Set<Integer> nonSelectedNodeIDs) {
        if (move.getType().equals("Inter")) {
            return validateInterMove(move, currentSolution, nonSelectedNodeIDs);
        } else if (move.getType().equals("Intra") && move.getIntraType().equals("Edge")) {
            return validateIntraEdgeMove(move, currentSolution);
        }

        return MoveValidityStatus.INVALID_REMOVE;
    }

    private void initMoveQueue(Solution currentSolution,
                                Set<Integer> nonSelectedNodes,
                                Set<Integer> allNodes){
        moveQueue.clear();
        moveSignatures.clear();
        skippedMoves.clear();

        List<Move> allMoves = getNeighborhood(currentSolution, nonSelectedNodes, allNodes);
        for (Move move : allMoves) {
            DeltaMove deltaMove = Move2DeltaMove(move, currentSolution);
            if (deltaMove != null) {
                String moveSignature = deltaMove.getSignature();
                if (!moveSignatures.contains(moveSignature)) {
                    moveQueue.offer(deltaMove);
                    moveSignatures.add(moveSignature);
                }
            }
        }

    }

    private Set<Integer> getAffectedNodes(DeltaMove move, int[] pathBeforeMove) {
        Set<Integer> affected = new HashSet<>();

        if (move.getType().equals("Inter")) {
            // For inter-route moves: the swapped nodes and their neighbors
            affected.add(move.getStartNodeID());
            affected.add(move.getEndNodeID());
            affected.add(move.getEdge1End());  // successor of old node
            affected.add(move.getEdge2End());  // predecessor of old node
        } else if (move.getType().equals("Intra") && move.getIntraType().equals("Edge")) {
            // For edge exchange: only the 4 edge endpoints are affected
            // Their incident edges changed, so moves involving them need recalculation
            // Interior nodes of reversed segment don't need move regeneration
            affected.add(move.getStartNodeID());  // edge1 start
            affected.add(move.getEdge1End());     // edge1 end
            affected.add(move.getEndNodeID());    // edge2 start  
            affected.add(move.getEdge2End());     // edge2 end
        }

        return affected;
    }

    private void generateNewMoves(Solution currentSolution,
                                  Set<Integer> affectedNodes,
                                  Set<Integer> nonSelectedNodes,
                                  Set<Integer> allNodes){
        Set<Integer> selectedNodeIDs = new HashSet<>(allNodes);
        selectedNodeIDs.removeAll(nonSelectedNodes);

        int[] currentPath = currentSolution.getPath();

        // Generate inter moves involving affected nodes
        // Only affected nodes need their inter-route moves recalculated
        for (int affectedNode : affectedNodes) {
            if (selectedNodeIDs.contains(affectedNode)) {
                // Generate inter moves: swap this node with outside nodes
                for (int outsideNode : nonSelectedNodes) {
                    int delta = deltaNodeSwap(affectedNode, outsideNode, currentSolution);
                    Move move = new Move("Inter", "-", affectedNode, outsideNode, delta);
                    DeltaMove deltaMove = Move2DeltaMove(move, currentSolution);

                    String signature = deltaMove.getSignature();
                    if (!moveSignatures.contains(signature)) {
                        moveQueue.offer(deltaMove);
                        moveSignatures.add(signature);
                    }
                }
            }
        }

        // Generate intra edge exchange moves involving affected nodes
        // Key optimization: only generate moves between pairs of affected nodes
        // or between one affected and one non-affected node
        List<Integer> affectedList = new ArrayList<>(affectedNodes);
        
        for (int i = 0; i < affectedList.size(); i++) {
            int affectedNode = affectedList.get(i);
            if (!selectedNodeIDs.contains(affectedNode)) continue;
            
            int affectedPos = findNodePosition(currentPath, affectedNode);
            if (affectedPos == -1) continue;

            // Generate moves with all other selected nodes (both affected and non-affected)
            for (int j = 0; j < currentPath.length - 1; j++) {
                int otherNode = currentPath[j];
                if (otherNode == affectedNode) continue;
                
                // Skip if edges are adjacent
                if (Math.abs(affectedPos - j) < 2) continue;
                if (affectedPos == 0 && j == currentPath.length - 2) continue;

                int delta = deltaEdgeExchange(affectedNode, otherNode, currentSolution);
                Move move = new Move("Intra", "Edge", affectedNode, otherNode, delta);
                DeltaMove deltaMove = Move2DeltaMove(move, currentSolution);

                String signature = deltaMove.getSignature();
                if (!moveSignatures.contains(signature)) {
                    moveQueue.offer(deltaMove);
                    moveSignatures.add(signature);
                }
            }
        }
    }

    @Override
    public Solution steepestLocalSearch(Solution currentSolution, Set<Integer> allNodeIDs) {
        Set<Integer> selectedNodeIDs = Arrays.stream(currentSolution.getPath())
                .boxed().collect(Collectors.toSet());
        Set<Integer> nonSelectedNodeIDs = new HashSet<>(allNodeIDs);
        nonSelectedNodeIDs.removeAll(selectedNodeIDs);

        initMoveQueue(currentSolution, nonSelectedNodeIDs, allNodeIDs);

        currentIteration = 0;

        while (!moveQueue.isEmpty()) {
//            DeltaMove peekMove = moveQueue.peek();
//            System.out.println("Iteration: " + currentIteration +
//                    ", Current Score: " + currentSolution.getScore() +
//                    ", Moves in Queue: " + moveQueue.size() +
//                    ", Best move delta: " + peekMove.getDelta() +
//                    ", Validity Check: " + checkMoveValidity(peekMove, currentSolution, nonSelectedNodeIDs));
            DeltaMove bestMove = moveQueue.poll();
            String signature = bestMove.getSignature();
            moveSignatures.remove(signature);


            MoveValidityStatus status = checkMoveValidity(bestMove, currentSolution,
                    nonSelectedNodeIDs);

            if (status == MoveValidityStatus.INVALID_REMOVE) {
                // Move is invalid, discard and continue
                continue;
            }

            if (status == MoveValidityStatus.VALID_SKIP) {
                skippedMoves.add(bestMove);
                continue;
            }

            if (status == MoveValidityStatus.RECALCULATE) {
                int newDelta;
                if (bestMove.getType().equals("Inter")) {
                    newDelta = deltaNodeSwap(bestMove.getStartNodeID(),
                            bestMove.getEndNodeID(),
                            currentSolution);
                } else if (bestMove.getType().equals("Intra") &&
                        bestMove.getIntraType().equals("Edge")) {
                    newDelta = deltaEdgeExchange(bestMove.getStartNodeID(),
                            bestMove.getEndNodeID(),
                            currentSolution);
                } else {
                    continue;
                }

                // Recreate move with current solution state to update edge info
                Move tempMove = new Move(
                        bestMove.getType(),
                        bestMove.getIntraType(),
                        bestMove.getStartNodeID(),
                        bestMove.getEndNodeID(),
                        newDelta
                );
                DeltaMove updatedMove = Move2DeltaMove(tempMove, currentSolution);
                
                if (updatedMove != null) {
                    String sig = updatedMove.getSignature();
                    if (!moveSignatures.contains(sig)) {
                        moveQueue.offer(updatedMove);
                        moveSignatures.add(sig);
                    }
                }
                continue;
            }

            // status == VALID_APPLY
            if (bestMove.getDelta() < 0) {
                Set<Integer> affectedNodes = getAffectedNodes(bestMove, currentSolution.getPath());

                currentSolution = applyMove(currentSolution, bestMove);

                // Update selected/non-selected sets for inter moves
                if (bestMove.getType().equals("Inter")) {
                    selectedNodeIDs.remove(bestMove.getStartNodeID());
                    selectedNodeIDs.add(bestMove.getEndNodeID());
                    nonSelectedNodeIDs.add(bestMove.getStartNodeID());
                    nonSelectedNodeIDs.remove(bestMove.getEndNodeID());
                }

                currentIteration++;
                currentSolution.setIterationCount(currentIteration);

                // Generate new moves for affected nodes only
                generateNewMoves(currentSolution, affectedNodes,
                        nonSelectedNodeIDs, allNodeIDs);

                // Reprocess skipped moves after solution update
                Set<DeltaMove> toReprocess = new HashSet<>(skippedMoves);
                skippedMoves.clear();
                for (DeltaMove skipped : toReprocess) {
                    MoveValidityStatus skippedStatus = checkMoveValidity(skipped, currentSolution, nonSelectedNodeIDs);
                    
                    if (skippedStatus == MoveValidityStatus.VALID_APPLY) {
                        String sig = skipped.getSignature();
                        if (!moveSignatures.contains(sig)) {
                            moveQueue.offer(skipped);
                            moveSignatures.add(sig);
                        }
                    } else if (skippedStatus == MoveValidityStatus.VALID_SKIP) {
                        skippedMoves.add(skipped);
                    } else if (skippedStatus == MoveValidityStatus.RECALCULATE) {
                        int newDelta;
                        if (skipped.getType().equals("Inter")) {
                            newDelta = deltaNodeSwap(skipped.getStartNodeID(), skipped.getEndNodeID(), currentSolution);
                        } else {
                            newDelta = deltaEdgeExchange(skipped.getStartNodeID(), skipped.getEndNodeID(), currentSolution);
                        }
                        DeltaMove updated = new DeltaMove(
                                skipped.getType(), skipped.getIntraType(),
                                skipped.getStartNodeID(), skipped.getEndNodeID(),
                                newDelta, skipped.getEdge1End(), skipped.getEdge2End(),
                                skipped.isEdge1Direction(), skipped.isEdge2Direction()
                        );
                        String sig = updated.getSignature();
                        if (!moveSignatures.contains(sig)) {
                            moveQueue.offer(updated);
                            moveSignatures.add(sig);
                        }
                    }
                    // INVALID_REMOVE: just don't add back
                }
            } else {
                // No improving move found (best delta >= 0)
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
