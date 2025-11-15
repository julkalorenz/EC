package main.java.models;

public class Edge {
    private int startNodeID;
    private int endNodeID;

    public Edge(int startNodeID, int endNodeID) {
        this.startNodeID = startNodeID;
        this.endNodeID = endNodeID;
    }

    public int getStartNodeID() {
        return startNodeID;
    }

    public int getEndNodeID() {
        return endNodeID;
    }

    public String getEdgeTxt() {
        return "(" + this.startNodeID + "," + this.endNodeID + ")";
    }

    public Edge reverse() {
        return new Edge(this.endNodeID, this.startNodeID);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Edge edge = (Edge) obj;
        return startNodeID == edge.startNodeID && endNodeID == edge.endNodeID;
    }

    @Override
    public int hashCode() {
        return 31 * startNodeID + endNodeID;
    }

}