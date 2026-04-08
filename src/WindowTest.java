import javax.swing.*;
import java.awt.*;
import java.io.FileWriter;

public class WindowTest {

    public static void openWindow(MLP mlp) {

        JFrame frame = new JFrame("Trainer");

        Canvas canvas = new Canvas();

        JRadioButton cBtn = new JRadioButton("C");
        JRadioButton oBtn = new JRadioButton("O");
        JRadioButton sBtn = new JRadioButton("S");

        ButtonGroup group = new ButtonGroup();
        group.add(cBtn);
        group.add(oBtn);
        group.add(sBtn);

        JButton reset = new JButton("Reset");
        JButton save = new JButton("Save Sample");
        JButton print = new JButton("Print Vector");
        JButton trainFileBtn = new JButton("Train from file");

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

            try (FileWriter fw = new FileWriter("training.txt", true)) {
                fw.write(line + "\n");
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }

            double[] input = canvas.getInputVector();
            double[] target = TrainingData.labelToTarget(label);

            mlp.trainSample(input, target, 0.1);
            mlp.saveWeights();

            System.out.println("Saved and trained: " + line);
        });
        
        trainFileBtn.addActionListener(e -> { // enables training thru the entire training set on demand

            String epochsStr = JOptionPane.showInputDialog("Epochs:", "1000");
            if (epochsStr == null) return;

            int epochs = Integer.parseInt(epochsStr);

            var samples = TrainingData.loadFromFile("training.txt");

            double[][] inputs = new double[samples.size()][64];
            double[][] targets = new double[samples.size()][3];

            for (int i = 0; i < samples.size(); i++) {
                inputs[i] = samples.get(i).input;
                targets[i] = TrainingData.labelToTarget(samples.get(i).label);
            }

            mlp.train(inputs, targets, epochs, 0.1);

            mlp.saveWeights();

            JOptionPane.showMessageDialog(null, "Training complete");
        });

        JPanel controls = new JPanel();
        controls.add(new JLabel("Label:"));
        controls.add(cBtn);
        controls.add(oBtn);
        controls.add(sBtn);
        controls.add(save);
        controls.add(reset);
        //controls.add(print);
        controls.add(trainFileBtn);

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