import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class CSVParser {
    private final String filePath;
    private final String delimiter;

    public static void main(String[] args) {
        String filePath = "data/TSPA.csv";
        String delimiter = ";";
        CSVParser parser = new CSVParser(filePath, delimiter);
        int[][] data = parser.readCSV();
        int lineNo = 1;
        for (int[] row : data) {
            System.out.print("Line " + lineNo + ": ");
            for (int value : row) {
                System.out.print(value + " | ");
            }
            System.out.println();
            lineNo++;
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

}

