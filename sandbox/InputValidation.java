import javax.swing.*;

public class InputValidation {
    public static void main(String[] args) {
        JFrame f = new JFrame("Validation Demo");
        f.setSize(400, 200);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTextField name = new JTextField(20);
        JTextField description = new JTextField(20);
        JButton add = new JButton("Add");

        add.addActionListener(e -> {
            String n = name.getText().trim();
            String d = description.getText().trim();

            // Validation 1: Required fields
            if (n.isEmpty() || d.isEmpty()) {
                JOptionPane.showMessageDialog(f, "Both fields required!",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validation 2: Length limit
            if (n.length() > 25 || d.length() > 80) {
                JOptionPane.showMessageDialog(f, "Input too long!",
                        "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(f, "Added: " + n,
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            name.setText("");
            description.setText("");
        });

        JPanel p = new JPanel();
        p.add(new JLabel("Name:"));
        p.add(name);
        p.add(new JLabel("Description:"));
        p.add(description);
        p.add(add);

        f.add(p);
        f.setVisible(true);
    }
}
