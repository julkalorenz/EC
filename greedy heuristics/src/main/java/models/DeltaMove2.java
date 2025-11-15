package main.java.models;

public class DeltaMove2 extends Move{

    private Edge old_edge1;
    private Edge old_edge2;
    private Edge new_edge1;
    private Edge new_edge2;
    
    // Structural metadata for validation
    private int pos1;          // Position in path for intra moves, or node position for inter moves
    private int pos2;          // Second position for intra moves
    private int predecessor;   // Predecessor node ID
    private int successor;     // Successor node ID
    private boolean segmentOrientation; // For 2-opt: segment orientation when move was created


    // Constructor
    public DeltaMove2(String type,
                     String intraType,
                     int startNodeID,
                     int endNodeID,
                     int delta,
                     Edge old_edge1,
                     Edge old_edge2,
                     Edge new_edge1,
                     Edge new_edge2,
                     int pos1,
                     int pos2,
                     int predecessor,
                     int successor,
                     boolean segmentOrientation
    ) {
        super(type, intraType, startNodeID, endNodeID, delta);
        this.old_edge1 = old_edge1;
        this.old_edge2 = old_edge2;
        this.new_edge1 = new_edge1;
        this.new_edge2 = new_edge2;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.predecessor = predecessor;
        this.successor = successor;
        this.segmentOrientation = segmentOrientation;
    }

    public Edge getOldEdge1() {
        return old_edge1;
    }
    public Edge getOldEdge2() {
        return old_edge2;
    }
    public Edge getNewEdge1() {
        return new_edge1;
    }
    public Edge getNewEdge2() {
        return new_edge2;
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
    
    public boolean getSegmentOrientation() {
        return segmentOrientation;
    }


    public String getSignature() {
        // Enhanced signature with structural information
        if (this.getType().equals("Intra") && this.getIntraType().equals("Edge")) {
            return "2opt|" + pos1 + "|" + pos2 + "|" +
                   old_edge1.getStartNodeID() + "|" + old_edge1.getEndNodeID() + "|" +
                   old_edge2.getStartNodeID() + "|" + old_edge2.getEndNodeID() + "|" +
                   predecessor + "|" + successor + "|" + segmentOrientation;
        } else if (this.getType().equals("Inter")) {
            return "Inter|" + pos1 + "|" + startNodeID + "|" + endNodeID + "|" +
                   predecessor + "|" + successor;
        }
        return this.getType() + "-" +
                this.old_edge1.getEdgeTxt() + "-" + this.old_edge2.getEdgeTxt() + 
                "->" + this.new_edge1.getEdgeTxt() + "-" + this.new_edge2.getEdgeTxt();
    }

}
