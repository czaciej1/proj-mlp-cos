import javax.swing.*;
import java.awt.*;

public class Main {

    private Canvas canvas;
    private PrevPanel preview;
    private JLabel predictionLabel;
    private MLP mlp; //Bat

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().createUI());
    }

    private void createUI() {

        JFrame frame = new JFrame("MLP - COS");

        canvas = new Canvas();
        preview = new PrevPanel();
        mlp = new MLP(64, 16, 8, 3); //Bat

        predictionLabel = new JLabel("Prediction: ?", SwingConstants.CENTER);
        predictionLabel.setFont(new Font("Arial", Font.BOLD, 20));

        JButton predictBtn = new JButton("Predict");
        JButton correctBtn = new JButton("Right");
        JButton wrongBtn = new JButton("Wrong");
        
        JButton resetBtn = new JButton("Reset");

        // actions
        
        resetBtn.addActionListener(e -> {
            canvas.resetCanvas();
            predictionLabel.setText("Prediction: ?");
            preview.setData(new double[8][8]); // clear preview
        });
        
        predictBtn.addActionListener(e -> updatePrediction());

        correctBtn.addActionListener(e -> {
            System.out.println("Marked as correct");
        });

        wrongBtn.addActionListener(e -> {
            System.out.println("Marked as wrong");
        });

        // right panel

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        preview.setPreferredSize(new Dimension(200, 200));

        rightPanel.add(preview);
        rightPanel.add(Box.createVerticalStrut(20));
        rightPanel.add(predictionLabel);
        rightPanel.add(Box.createVerticalStrut(20));
        rightPanel.add(predictBtn);
        rightPanel.add(Box.createVerticalStrut(10));
        //rightPanel.add(correctBtn);
        //rightPanel.add(Box.createVerticalStrut(10));
        //rightPanel.add(wrongBtn);
        //rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(resetBtn);
        rightPanel.add(Box.createVerticalStrut(40));

        // mode panel

        JPanel modePanel = new JPanel();

        JButton modeTest = new JButton("Testing");
        JButton modeTrain = new JButton("Training");
        
        modeTest.addActionListener(e->{
        	TestingMode.openWindow(mlp);
        });
        
        modeTrain.addActionListener(e -> {
        	WindowTest.openWindow(mlp);
        });

        modePanel.add(modeTrain);
        modePanel.add(modeTest);

        // layout

        frame.setLayout(new BorderLayout());
        frame.add(canvas, BorderLayout.CENTER);
        frame.add(rightPanel, BorderLayout.EAST);
        frame.add(modePanel, BorderLayout.NORTH);

        frame.setSize(900, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    // prediction simulation

    private void updatePrediction() {

        double[][] density = canvas.sampleTileDensity();
        preview.setData(density);

        double[] vec = canvas.getInputVector();

        // DEBUG
        double[] raw = mlp.predict(vec);

        System.out.print("Output: ");
        for (double v : raw) {
            System.out.printf("%.3f ", v);
        }
        System.out.println();

        // prediction index
        int predicted = mlp.predictClass(vec);

        // map to letters
        String result;

        switch (predicted) {
            case 0: result = "C"; break;
            case 1: result = "O"; break;
            case 2: result = "S"; break;
            default: result = "?";
        }

        predictionLabel.setText("Prediction: " + result);
    }
}