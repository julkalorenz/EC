package main.java.models;

public class Move {

    protected String type;
    protected String intraType;
    protected int startNodeID;
    protected int endNodeID;

    protected int delta;


    public Move(String type, String intraType, int startNodeID, int endNodeID, int delta) {
        this.type = type;
        this.intraType = intraType;
        this.startNodeID = startNodeID;
        this.endNodeID = endNodeID;
        this.delta = delta;

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
    public int getDelta() {
        return delta;
    }
    public void setDelta(int delta) {
        this.delta = delta;
    }

}
