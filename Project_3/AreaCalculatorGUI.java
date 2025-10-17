import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AreaCalculatorGUI extends JFrame implements ActionListener {
    private JComboBox<String> shapeSelector;
    private JLabel label1, label2, resultLabel;
    private JTextField field1, field2;
    private JButton calculateButton;

    public AreaCalculatorGUI() {
        setTitle("Area Calculator");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(6, 2, 10, 10));

        shapeSelector = new JComboBox<>(new String[]{"Select Shape", "Circle", "Rectangle", "Triangle"});
        label1 = new JLabel("Enter Value 1:");
        label2 = new JLabel("Enter Value 2:");
        field1 = new JTextField();
        field2 = new JTextField();
        calculateButton = new JButton("Calculate Area");
        resultLabel = new JLabel("Result: ");

        add(new JLabel("Choose Shape:"));
        add(shapeSelector);
        add(label1);
        add(field1);
        add(label2);
        add(field2);
        add(new JLabel(""));
        add(calculateButton);
        add(new JLabel(""));
        add(resultLabel);

        shapeSelector.addActionListener(this);
        calculateButton.addActionListener(this);

        field2.setVisible(false);
        label2.setVisible(false);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == shapeSelector) {
            String shape = (String) shapeSelector.getSelectedItem();

            switch (shape) {
                case "Circle":
                    label1.setText("Radius:");
                    label2.setVisible(false);
                    field2.setVisible(false);
                    break;

                case "Rectangle":
                    label1.setText("Length:");
                    label2.setText("Width:");
                    label2.setVisible(true);
                    field2.setVisible(true);
                    break;

                case "Triangle":
                    label1.setText("Base:");
                    label2.setText("Height:");
                    label2.setVisible(true);
                    field2.setVisible(true);
                    break;

                default:
                    label1.setText("Enter Value 1:");
                    label2.setVisible(false);
                    field2.setVisible(false);
                    break;
            }
        }

        if (source == calculateButton) {
            try {
                String shape = (String) shapeSelector.getSelectedItem();
                double val1 = Double.parseDouble(field1.getText());
                double area = 0;

                switch (shape) {
                    case "Circle":
                        area = Math.PI * val1 * val1;
                        break;
                    case "Rectangle":
                        double val2Rect = Double.parseDouble(field2.getText());
                        area = val1 * val2Rect;
                        break;
                    case "Triangle":
                        double val2Tri = Double.parseDouble(field2.getText());
                        area = 0.5 * val1 * val2Tri;
                        break;
                    default:
                        JOptionPane.showMessageDialog(this, "Please select a valid shape.");
                        return;
                }

                resultLabel.setText(String.format("Result: Area = %.2f", area));

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numeric values.");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AreaCalculatorGUI::new);
    }
}
