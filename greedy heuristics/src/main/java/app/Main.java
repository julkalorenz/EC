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

//        String dataset = "TSPA";
//        CSVParser parser = new CSVParser("src/main/data/" + dataset + ".csv", ";");
//
//
//        int[][] distanceMatrix = parser.getDistanceMatrix();
//        int[][] objectiveMatrix = parser.getObjectiveMatrix();
//        List<Node> nodes = parser.getNodes();
//        int[] costs = nodes.stream().mapToInt(Node::getCost).toArray();
//        GenericSolver solver = new LocalSearchSolver(
//                distanceMatrix,
//                objectiveMatrix,
//                costs,
//                nodes,
//                "Greedy",
//                "Edge",
//                "Greedy");
//        Experiment experiment = new Experiment(solver, dataset);
//
//        experiment.runExperiment();
//        experiment.printStats();

        String[] typeLS = {"Greedy", "Steepest"};
        //String[] typeLS = {"Steepest"};
        String[] neighborhoodLS = {"Node", "Edge"};
        String[] initialSolutionLS = {"Greedy", "Random"};
        String[] datasets = {"TSPA", "TSPB"};
        for (String type : typeLS) {
            for (String neighborhood : neighborhoodLS) {
                for (String initialSolution : initialSolutionLS) {
                    for (String dataset: datasets) {
                        CSVParser parser = new CSVParser("src/main/data/" + dataset + ".csv", ";");
                        int[][] distanceMatrix = parser.getDistanceMatrix();
                        int[][] objectiveMatrix = parser.getObjectiveMatrix();
                        List<Node> nodes = parser.getNodes();
                        int[] costs = nodes.stream().mapToInt(Node::getCost).toArray();
                        System.out.println("Running Local Search with Type: " + type + ", Neighborhood: " + neighborhood
                                + ", Initial Solution: " + initialSolution + " on Dataset: " + dataset);
                        GenericSolver solver = new LocalSearchSolver(
                                distanceMatrix,
                                objectiveMatrix,
                                costs,
                                nodes,
                                type,
                                neighborhood,
                                initialSolution);
                        Experiment experiment = new Experiment(solver, dataset);
                        experiment.runExperiment();
                        experiment.printStats();
                        System.out.println("--------------------------------------------------");
                    }
                }
            }
        }
    }
}
