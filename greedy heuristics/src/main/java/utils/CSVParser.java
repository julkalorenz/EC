package main.java.utils;

import main.java.models.Node;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class CSVParser {
    private final String filePath;
    private final String delimiter;

    public static void main(String[] args) {
        String filePath = "src/main/data/TSPA.csv";
        String delimiter = ";";
        CSVParser parser = new CSVParser(filePath, delimiter);
        List<Node> nodes = parser.getNodes();
        for (Node node : nodes) {
            System.out.println("Node ID: " + node.getId() + ", X: " + node.getX() + ", Y: " + node.getY()+", Cost: " + node.getCost());
        }

    }

    public CSVParser(String filePath, String delimiter) {
        this.filePath = filePath;
        this.delimiter = delimiter;
    }

    public int[][] readCSV() {
        String line;
        List<int[]> data = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            while((line = br.readLine()) != null) {
                String[] values = line.split(delimiter);
                int[] row = new int[values.length];
                for (int i = 0; i < values.length; i++) {
                    row[i] = Integer.parseInt(values[i]);
                }
                data.add(row);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        int[][] arrayData = new int[data.size()][];
        return data.toArray(arrayData);
    }

    public List<Node> getNodes() {
        int[][] data = readCSV();
        List<Node> nodes = new ArrayList<>();
        int index = 0;
        for (int[] row : data) {
            if (row.length >= 3) {
                Node node = new Node(index, row[0], row[1], row[2]);
                nodes.add(node);
            }
            index++;
        }
        return nodes;
    }

    public int[][] getDistanceMatrix() {
        int[][] data = readCSV();
        int size = data.length;
        int[][] distanceMatrix = new int[size][size];

        for (int i =0; i < size; i++){
            int x1 = data[i][0];
            int y1 = data[i][1];

            for (int j =0; j < size; j++){
                int x2 = data[j][0];
                int y2 = data[j][1];
                int distance = (int) Math.round(Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2)));
                distanceMatrix[i][j] = distance;
            }
        }
        return distanceMatrix;
    }

    public int[][] getObjectiveMatrix() {
        int[][] data = readCSV();
        int[][] distanceMatrix = getDistanceMatrix();
        int[][] objectiveMatrix = new int[data.length][data.length];
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data.length; j++) {
                if (i != j) {
                    objectiveMatrix[i][j] = distanceMatrix[i][j] + data[j][2];
                } else {
                    objectiveMatrix[i][j] = 0;
                }
            }
        }
        return objectiveMatrix;
    }

    public int[] getCosts() {
        int[][] data = readCSV();
        int[] costs = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            costs[i] = data[i][2];
        }
        return costs;
    }
}

