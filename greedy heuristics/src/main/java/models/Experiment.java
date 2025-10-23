package main.java.models;

import main.java.solver.GenericSolver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;



public class Experiment {
    private GenericSolver solver;
    private Solution bestSolution;
    private float[] solutionTimes;
    private int[] solutionScores;
    private String datasetName;
    private int[] solutionIters;

    public Experiment(GenericSolver solver, String datasetName) {
        this.solver = solver;
        this.datasetName = datasetName;
    }

    public Solution getBestSolution() {
        return bestSolution;
    }
    public float[] getSolutionTimes() {
        return solutionTimes;
    }
    public int[] getSolutionScores() {
        return solutionScores;
    }

    /**
     * Function to run the experiment by solving the problem from each possible starting node.
     * It records the time taken and score for each solution, and keeps track of the best solution found.
     */
    public void runExperiment() {
        int nodesCount = solver.getObjectiveMatrix().length;
        solutionTimes = new float[nodesCount];
        solutionScores = new int[nodesCount];
        solutionIters = new int[nodesCount];
        bestSolution = null;

        int barWidth = 40;

        for (int startNodeID = 0; startNodeID < nodesCount; startNodeID++) {
            long startTime = System.nanoTime();
            Solution solution = solver.getSolution(startNodeID);
            long endTime = System.nanoTime();

            float durationInSeconds = (endTime - startTime) / 1_000_000_000.0f;
            solutionTimes[startNodeID] = durationInSeconds;
            int score = solution.getScore();
            solutionScores[startNodeID] = score;
            solutionIters[startNodeID] = solution.getIterationCount();

            if (bestSolution == null || score < bestSolution.getScore()) {
                bestSolution = solution;
            }

            // --- Progress bar update ---
            double progress = (startNodeID + 1) / (double) nodesCount;
            int filled = (int) (barWidth * progress);
            int percent = (int) (progress * 100);

            String bar = "█".repeat(filled) + "-".repeat(barWidth - filled);
            System.out.print("\rProgress: [" + bar + "] " + percent + "%");
            System.out.flush();
        }
        System.out.println("\nDone!");
    }

    /**
     * Print statistics of experiment such as min, max and average time and score
     * Visualize best solution found
     */
    public void printStats(){
        String baseFolder = "results/" + solver.getMethodName() + "/";
//        bestSolution.displaySolution();
        bestSolution.saveAsImage(baseFolder + datasetName + "_best_solution.png");
        bestSolution.savePath(baseFolder + datasetName + "_best_path.txt");
        float minTime = Float.MAX_VALUE;
        float maxTime = Float.MIN_VALUE;
        float totalTime = 0;

        int minScore = Integer.MAX_VALUE;
        int maxScore = Integer.MIN_VALUE;
        int totalScore = 0;

        int minIters = Integer.MAX_VALUE;
        int maxIters = Integer.MIN_VALUE;
        int totalIters = 0;

        for (float time : solutionTimes) {
            if (time < minTime) minTime = time;
            if (time > maxTime) maxTime = time;
            totalTime += time;
        }

        for (int score : solutionScores) {
            if (score < minScore) minScore = score;
            if (score > maxScore) maxScore = score;
            totalScore += score;
        }

        for (int iters : solutionIters) {
            if (iters < minIters) minIters = iters;
            if (iters > maxIters) maxIters = iters;
            totalIters += iters;
        }




        float avgTime = totalTime / solutionTimes.length;
        float avgScore = (float) totalScore / solutionScores.length;
        float avgIters = (float) totalIters / solutionIters.length;


        System.out.println("Time (seconds): Min = " + minTime + ", Max = " + maxTime + ", Avg = " + avgTime);
        System.out.println("Score: Min = " + minScore + ", Max = " + maxScore + ", Avg = " + avgScore);
        System.out.println("Iterations: Min = " + minIters + ", Max = " + maxIters + ", Avg = " + (totalIters / solutionIters.length));

        writeResultsToFile(baseFolder, minTime, maxTime, avgTime, minScore, maxScore, avgScore, minIters, maxIters, avgIters);
    }

    private void writeResultsToFile(String baseFolder,
                                    float minTime, float maxTime, float avgTime,
                                    int minScore, int maxScore, float avgScore,
                                    int minIters, int maxIters, float avgIters) {

        File folder = new File(baseFolder);
        folder.mkdirs();

        File resultsFile = new File(folder, "results.txt");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(resultsFile, true))) {
            writer.write(datasetName + ":\n");
            writer.write(String.format(Locale.US,
                    "Time (seconds): %.4f (%.4f, %.4f)%n",
                    avgTime, minTime, maxTime));
            writer.write(String.format(Locale.US,
                    "Score: %.2f (%d, %d)%n",
                    avgScore, minScore, maxScore));
            writer.write(String.format(Locale.US,
                    "Iterations: %.2f (%d, %d)%n",
                    avgIters, minIters, maxIters));
            writer.write("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}