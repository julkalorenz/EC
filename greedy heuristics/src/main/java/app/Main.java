package main.java.app;

import java.util.List;
import java.util.Random;

import main.java.solver.*;

import main.java.models.Experiment;
import main.java.models.Solution;
import main.java.models.Node;
import main.java.utils.CSVParser;

/**
 * Main class will create new instances of solvers that inherit from GenericSolver
 * They will run the experiment and display the best solution found
 * Also display stats such as min, max and average time and score
 */
public class Main{
    public static void main(String[] args) {


        String dataset = "TSPA";
        //CSVParser parser = new CSVParser("src/main/data/" + dataset + ".csv", ";");
        CSVParser parser = new CSVParser("greedy heuristics/src/main/data/" + dataset + ".csv", ";");


        int[][] distanceMatrix = parser.getDistanceMatrix();
        int[][] objectiveMatrix = parser.getObjectiveMatrix();
        List<Node> nodes = parser.getNodes();
        int[] costs = nodes.stream().mapToInt(Node::getCost).toArray();

        // Example: assume generic solver is not abstract
        GenericSolver solver = new NN2Solver(distanceMatrix,objectiveMatrix, costs, nodes);

        Experiment experiment = new Experiment(solver, dataset);

        experiment.runExperiment();
        experiment.printStats();

    }
}
