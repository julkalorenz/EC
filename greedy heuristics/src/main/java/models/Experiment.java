package main.java.models;

import main.java.solver.GenericSolver;


public class Experiment {
    private GenericSolver solver;
    private Solution bestSolution;
    private float[] solutionTimes;
    private int[] solutionScores;

    public Experiment(GenericSolver solver) {
        this.solver = solver;
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
        bestSolution.displaySolution();
        float minTime = Float.MAX_VALUE;
        float maxTime = Float.MIN_VALUE;
        float totalTime = 0;

        int minScore = Integer.MAX_VALUE;
        int maxScore = Integer.MIN_VALUE;
        int totalScore = 0;

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

        float avgTime = totalTime / solutionTimes.length;
        float avgScore = (float) totalScore / solutionScores.length;

        System.out.println("Time (seconds): Min = " + minTime + ", Max = " + maxTime + ", Avg = " + avgTime);
        System.out.println("Score: Min = " + minScore + ", Max = " + maxScore + ", Avg = " + avgScore);
    }




}
