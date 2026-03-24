import java.util.Random;
import java.io.*;

public class MLP {

	private static final String WEIGHTS_FILE = "mlp_weights.txt";
	
	private int totalEpochsTrained = 0;
	private static final String EPOCH_FILE = "epoch_count.txt";
	
    private final int inputSize;
    private final int hidden1Size;
    private final int hidden2Size;
    private final int outputSize;

    private final double[][] w1;
    private final double[] b1;

    private final double[][] w2;
    private final double[] b2;

    private final double[][] w3;
    private final double[] b3;

    private final Random random = new Random();

    public MLP(int inputSize, int hidden1Size, int hidden2Size, int outputSize) {
        this.inputSize = inputSize;
        this.hidden1Size = hidden1Size;
        this.hidden2Size = hidden2Size;
        this.outputSize = outputSize;

        w1 = new double[hidden1Size][inputSize];
        b1 = new double[hidden1Size];

        w2 = new double[hidden2Size][hidden1Size];
        b2 = new double[hidden2Size];

        w3 = new double[outputSize][hidden2Size];
        b3 = new double[outputSize];

        if (!loadWeights()) {
            System.out.println("No weights file found → initializing randomly");
            initWeights();
        } else {
            System.out.println("Weights loaded from file");
        }
        loadEpochCount(); // Epoch counter functionality, should keep for report
        //initWeights(); // Outdated
    }

    private void initWeights() {
        initMatrix(w1);
        initMatrix(w2);
        initMatrix(w3);
    }

    private void initMatrix(double[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                matrix[i][j] = (random.nextDouble() * 2.0 - 1.0) * 0.5;
            }
        }
    }

    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    private double sigmoidDerivative(double y) {
        return y * (1.0 - y);
    }

    public double[] forward(double[] input) {
        double[] hidden1 = new double[hidden1Size];
        double[] hidden2 = new double[hidden2Size];
        double[] output = new double[outputSize];

        for (int i = 0; i < hidden1Size; i++) {
            double sum = b1[i];
            for (int j = 0; j < inputSize; j++) {
                sum += w1[i][j] * input[j];
            }
            hidden1[i] = sigmoid(sum);
        }

        for (int i = 0; i < hidden2Size; i++) {
            double sum = b2[i];
            for (int j = 0; j < hidden1Size; j++) {
                sum += w2[i][j] * hidden1[j];
            }
            hidden2[i] = sigmoid(sum);
        }

        for (int i = 0; i < outputSize; i++) {
            double sum = b3[i];
            for (int j = 0; j < hidden2Size; j++) {
                sum += w3[i][j] * hidden2[j];
            }
            output[i] = sigmoid(sum);
        }

        return output;
    }

    public void trainSample(double[] input, double[] target, double learningRate) {
        double[] hidden1 = new double[hidden1Size];
        double[] hidden2 = new double[hidden2Size];
        double[] output = new double[outputSize];

        for (int i = 0; i < hidden1Size; i++) {
            double sum = b1[i];
            for (int j = 0; j < inputSize; j++) {
                sum += w1[i][j] * input[j];
            }
            hidden1[i] = sigmoid(sum);
        }

        for (int i = 0; i < hidden2Size; i++) {
            double sum = b2[i];
            for (int j = 0; j < hidden1Size; j++) {
                sum += w2[i][j] * hidden1[j];
            }
            hidden2[i] = sigmoid(sum);
        }

        for (int i = 0; i < outputSize; i++) {
            double sum = b3[i];
            for (int j = 0; j < hidden2Size; j++) {
                sum += w3[i][j] * hidden2[j];
            }
            output[i] = sigmoid(sum);
        }

        double[] deltaOutput = new double[outputSize];
        for (int i = 0; i < outputSize; i++) {
            double error = target[i] - output[i];
            deltaOutput[i] = error * sigmoidDerivative(output[i]);
        }

        double[] deltaHidden2 = new double[hidden2Size];
        for (int i = 0; i < hidden2Size; i++) {
            double error = 0.0;
            for (int j = 0; j < outputSize; j++) {
                error += w3[j][i] * deltaOutput[j];
            }
            deltaHidden2[i] = error * sigmoidDerivative(hidden2[i]);
        }

        double[] deltaHidden1 = new double[hidden1Size];
        for (int i = 0; i < hidden1Size; i++) {
            double error = 0.0;
            for (int j = 0; j < hidden2Size; j++) {
                error += w2[j][i] * deltaHidden2[j];
            }
            deltaHidden1[i] = error * sigmoidDerivative(hidden1[i]);
        }

        for (int i = 0; i < outputSize; i++) {
            for (int j = 0; j < hidden2Size; j++) {
                w3[i][j] += learningRate * deltaOutput[i] * hidden2[j];
            }
            b3[i] += learningRate * deltaOutput[i];
        }

        for (int i = 0; i < hidden2Size; i++) {
            for (int j = 0; j < hidden1Size; j++) {
                w2[i][j] += learningRate * deltaHidden2[i] * hidden1[j];
            }
            b2[i] += learningRate * deltaHidden2[i];
        }

        for (int i = 0; i < hidden1Size; i++) {
            for (int j = 0; j < inputSize; j++) {
                w1[i][j] += learningRate * deltaHidden1[i] * input[j];
            }
            b1[i] += learningRate * deltaHidden1[i];
        }
    }

    public void train(double[][] inputs, double[][] targets, int epochs, double learningRate) {
        for (int epoch = 0; epoch < epochs; epoch++) {
            for (int i = 0; i < inputs.length; i++) {
                trainSample(inputs[i], targets[i], learningRate);
            }
            totalEpochsTrained++;
        }
        saveEpochCount();
    }
    public void saveWeights() { // saving weights to file
        try (PrintWriter pw = new PrintWriter(new FileWriter(WEIGHTS_FILE))) {

            saveMatrix(pw, w1);
            saveVector(pw, b1);

            saveMatrix(pw, w2);
            saveVector(pw, b2);

            saveMatrix(pw, w3);
            saveVector(pw, b3);

            System.out.println("Weights saved.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void saveMatrix(PrintWriter pw, double[][] m) {
        for (double[] row : m) {
            for (double v : row) {
                pw.print(v + " ");
            }
            pw.println();
        }
    }

    private void saveVector(PrintWriter pw, double[] v) {
        for (double x : v) {
            pw.print(x + " ");
        }
        pw.println();
    }
    
    public boolean loadWeights() { // loading weights from file

        File file = new File(WEIGHTS_FILE);
        if (!file.exists()) return false;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            loadMatrix(br, w1);
            loadVector(br, b1);

            loadMatrix(br, w2);
            loadVector(br, b2);

            loadMatrix(br, w3);
            loadVector(br, b3);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    private void loadMatrix(BufferedReader br, double[][] m) throws IOException {
        for (int i = 0; i < m.length; i++) {
            String[] parts = br.readLine().trim().split("\\s+");
            for (int j = 0; j < m[i].length; j++) {
                m[i][j] = Double.parseDouble(parts[j]);
            }
        }
    }

    private void loadVector(BufferedReader br, double[] v) throws IOException {
        String[] parts = br.readLine().trim().split("\\s+");
        for (int i = 0; i < v.length; i++) {
            v[i] = Double.parseDouble(parts[i]);
        }
    }
    
    private void loadEpochCount() { // Epoch counter functionality.
        File file = new File(EPOCH_FILE);

        if (!file.exists()) {
            totalEpochsTrained = 0;
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            totalEpochsTrained = Integer.parseInt(br.readLine().trim());
        } catch (Exception e) {
            e.printStackTrace();
            totalEpochsTrained = 0;
        }
    }
    
    private void saveEpochCount() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(EPOCH_FILE))) {
            pw.println(totalEpochsTrained);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public int getTotalEpochsTrained() {
        return totalEpochsTrained;
    }

    public int predictClass(double[] input) {
        double[] output = forward(input);

        int bestIndex = 0;
        for (int i = 1; i < output.length; i++) {
            if (output[i] > output[bestIndex]) {
                bestIndex = i;
            }
        }

        return bestIndex;
    }

    public double[] predict(double[] input) {
        return forward(input);
    }
}