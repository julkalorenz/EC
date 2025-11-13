package main.java.models;

public class DeltaMove extends Move{
    private int edge1Start;
    private int edge1End;
    private int edge2Start;
    private int edge2End;

    private boolean edge1Direction;
    private boolean edge2Direction;


    // Constructor
    public DeltaMove(String type,
                     String intraType,
                     int startNodeID,
                     int endNodeID,
                     int delta,
                     int edge1End,
                     int edge2End,
                     boolean edge1Dir,
                     boolean edge2Dir) {
        super(type, intraType, startNodeID, endNodeID, delta);
        this.edge1Start = startNodeID;
        this.edge1End = edge1End;
        this.edge2Start = endNodeID;
        this.edge2End = edge2End;
        this.edge1Direction = edge1Dir;
        this.edge2Direction = edge2Dir;
    }

    public int getEdge1Start() {
        return edge1Start;
    }
    public int getEdge1End() {
        return edge1End;
    }
    public int getEdge2Start() {
        return edge2Start;
    }
    public int getEdge2End() {
        return edge2End;
    }
    public boolean isEdge1Direction() {
        return edge1Direction;
    }
    public boolean isEdge2Direction() {
        return edge2Direction;
    }

    public String getSignature() {
        if (type.equals("Inter")) {
            return type + "-" + edge1Start + "-" + edge1End + "-" +
                    edge2Start + "-" + edge2End;
        }
        return type + "-" + intraType + "-" + Math.min(edge1Start, edge2Start) + "-" +
                Math.max(edge1Start, edge2Start);
    }
}
