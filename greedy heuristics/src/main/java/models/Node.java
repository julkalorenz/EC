package main.java.models;

public class Node {

    private int id;
    private int cost;
    private int x;
    private int y;

    public Node(int id, int x, int y, int cost) {
        this.id = id;
        this.cost = cost;
        this.x = x;
        this.y = y;
    }
    public int getId() {
        return id;
    }
    public int getCost() {
        return cost;
    }
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }

}
