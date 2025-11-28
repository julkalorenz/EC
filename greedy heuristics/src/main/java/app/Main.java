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

        String[] datasets = {"TSPA", "TSPB"};
        float[] stoppingTimes = {15.9396f, 16.1847f};
        boolean[] useLSAfterPerturb = {true, false};
        for (String data: datasets) {
            for (boolean useLS: useLSAfterPerturb){
                float stoppingTime;
                if (data.equals("TSPA")) {
                    stoppingTime = stoppingTimes[0];
                } else {
                    stoppingTime = stoppingTimes[1];
                }
                System.out.println("Running Large Neighborhood Search on Dataset: " + data +
                        " with stopping time: " + stoppingTime +
                        " and useLSAfterPerturb: " + useLS);
                CSVParser parser = new CSVParser("src/main/data/" + data + ".csv", ";");
                int[][] distanceMatrix = parser.getDistanceMatrix();
                int[][] objectiveMatrix = parser.getObjectiveMatrix();
                List<Node> nodes = parser.getNodes();
                int[] costs = nodes.stream().mapToInt(Node::getCost).toArray();
                GenericSolver solver = new LargeNeighborhoodSearchSolver(
                        distanceMatrix,
                        objectiveMatrix,
                        costs,
                        nodes,
                        stoppingTime,
                        0.3,
                        useLS
                );
                solver.setMethodName("LNS_" + (useLS ? "WithLS" : "NoLS"));
                Experiment experiment = new Experiment(solver, data, 20);
                experiment.runExperiment();
                experiment.printStats();
            }
        }


//        String[] typeLS = {"Greedy", "Steepest"};
//        //String[] typeLS = {"Steepest"};
//        String[] neighborhoodLS = {"Node", "Edge"};
//        String[] initialSolutionLS = {"Greedy", "Random"};
//        String[] datasets = {"TSPA", "TSPB"};
//        for (String type : typeLS) {
//            for (String neighborhood : neighborhoodLS) {
//                for (String initialSolution : initialSolutionLS) {
//                    for (String dataset: datasets) {
//                        CSVParser parser = new CSVParser("src/main/data/" + dataset + ".csv", ";");
//                        int[][] distanceMatrix = parser.getDistanceMatrix();
//                        int[][] objectiveMatrix = parser.getObjectiveMatrix();
//                        List<Node> nodes = parser.getNodes();
//                        int[] costs = nodes.stream().mapToInt(Node::getCost).toArray();
//                        System.out.println("Running Local Search with Type: " + type + ", Neighborhood: " + neighborhood
//                                + ", Initial Solution: " + initialSolution + " on Dataset: " + dataset);
//                        GenericSolver solver = new LocalSearchSolver(
//                                distanceMatrix,
//                                objectiveMatrix,
//                                costs,
//                                nodes,
//                                type,
//                                neighborhood,
//                                initialSolution);
//                        Experiment experiment = new Experiment(solver, dataset);
//                        experiment.runExperiment();
//                        experiment.printStats();
//                        System.out.println("--------------------------------------------------");
//                    }
//                }
//            }
//        }
    }
}
