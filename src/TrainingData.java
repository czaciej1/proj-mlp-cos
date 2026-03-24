import java.io.*;
import java.util.*;

public class TrainingData {

    public static class Sample {
        public String label;
        public double[] input;

        public Sample(String label, double[] input) {
            this.label = label;
            this.input = input;
        }
    }

    public static List<Sample> loadFromFile(String path) {

        List<Sample> samples = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {

            String line;

            while ((line = br.readLine()) != null) {

                String[] parts = line.trim().split("\\s+");

                if (parts.length < 65) continue; // skip invalid lines

                String label = parts[0];
                double[] input = new double[64];

                for (int i = 0; i < 64; i++) {
                    input[i] = Double.parseDouble(parts[i + 1]);
                }

                samples.add(new Sample(label, input));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return samples;
    }
    public static double[] labelToTarget(String label) {

        double[] target = new double[3];

        switch (label) {
            case "C": target[0] = 1; break;
            case "O": target[1] = 1; break;
            case "S": target[2] = 1; break;
        }

        return target;
    }
}