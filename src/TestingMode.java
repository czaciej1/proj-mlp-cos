import javax.swing.*;
import java.awt.*;
import java.io.FileWriter;

public class TestingMode {

    public static void openWindow(MLP mlp) {

        JFrame frame = new JFrame("Tester");

        Canvas canvas = new Canvas();

        JRadioButton cBtn = new JRadioButton("C");
        JRadioButton oBtn = new JRadioButton("O");
        JRadioButton sBtn = new JRadioButton("S");

        ButtonGroup group = new ButtonGroup();
        group.add(cBtn);
        group.add(oBtn);
        group.add(sBtn);

        JButton reset = new JButton("Reset");
        JButton save = new JButton("Add to set");
        JButton print = new JButton("Print Vector");
        JButton testFileBtn = new JButton("Test from file");

        reset.addActionListener(e -> canvas.resetCanvas());

        print.addActionListener(e -> {
            double[] v = canvas.getInputVector();
            for (double d : v)
                System.out.printf("%.2f ", d);
            System.out.println();
        });

        save.addActionListener(e -> {

            String label = null;

            if (cBtn.isSelected()) label = "C";
            else if (oBtn.isSelected()) label = "O";
            else if (sBtn.isSelected()) label = "S";

            if (label == null) {
                System.out.println("Select a label");
                return;
            }

            String line = canvas.exportAsLabeledString(label);

            try (FileWriter fw = new FileWriter("testing.txt", true)) {
                fw.write(line + "\n");
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }



            System.out.println("Saved sample: " + line);
        });
        
        testFileBtn.addActionListener(e -> { // Runs testing once.

            var samples = TrainingData.loadFromFile("testing.txt");

            if (samples.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No testing data found.");
                return;
            }

            int total = samples.size();
            int correct = 0;

            // per-class stats
            int[] classTotal = new int[3];   // C, O, S
            int[] classCorrect = new int[3];

            for (int i = 0; i < samples.size(); i++) {

                double[] input = samples.get(i).input;
                String label = samples.get(i).label;

                int predicted = mlp.predictClass(input);

                int actual = -1;
                switch (label) {
                    case "C": actual = 0; break;
                    case "O": actual = 1; break;
                    case "S": actual = 2; break;
                }

                if (actual == -1) continue;

                classTotal[actual]++;

                if (predicted == actual) {
                    correct++;
                    classCorrect[actual]++;
                }
            }

            double overallAcc = (double) correct / total;

            // per-class accuracy
            double accC = classTotal[0] == 0 ? 0 : (double) classCorrect[0] / classTotal[0];
            double accO = classTotal[1] == 0 ? 0 : (double) classCorrect[1] / classTotal[1];
            double accS = classTotal[2] == 0 ? 0 : (double) classCorrect[2] / classTotal[2];

            String result = "Testing complete\n\n"
                    + "Overall: " + String.format("%.2f%%", overallAcc * 100) + "\n\n"
                    + "Per-class accuracy:\n"
                    + "C: " + String.format("%.2f%%", accC * 100) + "\n"
                    + "O: " + String.format("%.2f%%", accO * 100) + "\n"
                    + "S: " + String.format("%.2f%%", accS * 100) + "\n\n"
                    + "Correct: " + correct + "/" + total;

            JOptionPane.showMessageDialog(null, result);
        });

        JPanel controls = new JPanel();
        controls.add(new JLabel("Label:"));
        controls.add(cBtn);
        controls.add(oBtn);
        controls.add(sBtn);
        controls.add(save);
        controls.add(reset);
        //controls.add(print);
        controls.add(testFileBtn);

        frame.setLayout(new BorderLayout());
        frame.add(canvas, BorderLayout.CENTER);
        frame.add(controls, BorderLayout.SOUTH);

        frame.setSize(800, 850);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        MLP mlp = new MLP(64, 16, 8, 3);
        openWindow(mlp);
    }
}