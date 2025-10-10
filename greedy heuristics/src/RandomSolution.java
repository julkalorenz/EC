public class RandomSolution {

    private CSVParser parser;

//    choose 50% of the nodes for the cycle - if odd ceil
//    distance between nodes = Euclidean distance rounded mathematically
//    start from a random node and choose a random node until there are 50% of nodes selected
    public static void main(String[] args) {
        String filePath = "data/TSPA.csv";
        String delimiter = ";";
        CSVParser parser = new CSVParser(filePath, delimiter);
        int[][] data = parser.readCSV();

        RandomSolution randomSolution = new RandomSolution();
        int[][] cycle = randomSolution.generateRandomSolution(data);

        int lineNo = 1;
        for (int[] row : cycle) {
            System.out.print("Line " + lineNo + ": ");
            for (int value : row) {
                System.out.print(value + " | ");
            }
            System.out.println();
            lineNo++;
        }

    }

    public int[][] generateRandomSolution(int[][] nodes) {
        int totalNodes = nodes.length;
        int nodesInCycle = (int) Math.ceil(totalNodes / 2.0);

        boolean[] selected = new boolean[totalNodes];
        int[][] cycle = new int[nodesInCycle + 1][3];

        int count = 0;

        while (count < nodesInCycle) {
            int randomIndex = (int) (Math.random() * totalNodes);
            if (!selected[randomIndex]) {
                selected[randomIndex] = true;
                cycle[count] = nodes[randomIndex];
                count++;
            }
        }
        // ensure the cycle starts and ends at the same node
        cycle[nodesInCycle] = cycle[0];
        return cycle;
    }

}
