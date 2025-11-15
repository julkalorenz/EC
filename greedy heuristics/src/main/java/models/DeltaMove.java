package main.java.models;

public class DeltaMove extends Move{

    private Edge old_edge1;
    private Edge old_edge2;
    
    // Structural metadata for validation
    private int pos1;          // Position in path for intra moves, or node position for inter moves
    private int pos2;          // Second position for intra moves
    private int predecessor;   // Predecessor node ID
    private int successor;     // Successor node ID


    // Constructor
    public DeltaMove(String type,
                     String intraType,
                     int startNodeID,
                     int endNodeID,
                     int delta,
                     Edge old_edge1,
                     Edge old_edge2,
                     int pos1,
                     int pos2,
                     int predecessor,
                     int successor
    ) {
        super(type, intraType, startNodeID, endNodeID, delta);
        this.old_edge1 = old_edge1;
        this.old_edge2 = old_edge2;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.predecessor = predecessor;
        this.successor = successor;
    }

    public Edge getOldEdge1() {
        return old_edge1;
    }
    public Edge getOldEdge2() {
        return old_edge2;
    }
    
    public int getPos1() {
        return pos1;
    }
    
    public int getPos2() {
        return pos2;
    }
    
    public int getPredecessor() {
        return predecessor;
    }
    
    public int getSuccessor() {
        return successor;
    }

    public String getSignature() {
        // Enhanced signature with structural information
        if (this.getType().equals("Intra") && this.getIntraType().equals("Edge")) {
            return "2opt|" + pos1 + "|" + pos2 + "|" +
                   old_edge1.getStartNodeID() + "|" + old_edge1.getEndNodeID() + "|" +
                   old_edge2.getStartNodeID() + "|" + old_edge2.getEndNodeID() + "|" +
                   predecessor + "|" + successor;
        } else if (this.getType().equals("Inter")) {
            return "Inter|" + pos1 + "|" + startNodeID + "|" + endNodeID + "|" +
                   predecessor + "|" + successor;
        }
        return this.getType() + "-" +
                this.old_edge1.getEdgeTxt() + "-" + this.old_edge2.getEdgeTxt();
    }

}
