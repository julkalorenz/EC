package main.java.models;

public class Move {

    private String type;
    private String intraType;
    private int startNodeID;
    private int endNodeID;

    public Move(String type, String intraType, int startNodeID, int endNodeID) {
        this.type = type;
        this.intraType = intraType;
        this.startNodeID = startNodeID;
        this.endNodeID = endNodeID;
    }
    public String getType() {
        return type;
    }
    public String getIntraType() {
        return intraType;
    }
    public int getStartNodeID() {
        return startNodeID;
    }
    public int getEndNodeID() {
        return endNodeID;
    }

}
