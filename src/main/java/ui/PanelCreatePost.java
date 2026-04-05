package main.java.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import main.java.model.Post;

public class PanelCreatePost extends JPanel {
    private static final int FORM_WIDTH = 520;
    private static final Color PLACEHOLDER_COLOR = Color.LIGHT_GRAY;
    private static final Color INPUT_COLOR = Color.BLACK;
    private static final double TITLE_GAP_INCHES = 0.5;

    // Effects: creates the create-post screen with a button that returns the user
    // to the home page when clicked, plus text inputs for the post title and body.
    public PanelCreatePost(Runnable onBack, Consumer<Post> onDone) {
        setLayout(new BorderLayout(0, 24));
        setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        JButton backButton = new JButton("X");
        backButton.addActionListener(event -> onBack.run());

        JButton doneButton = new JButton("✓");

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        formPanel.setMaximumSize(new Dimension(FORM_WIDTH, Integer.MAX_VALUE));
        formPanel.setPreferredSize(new Dimension(FORM_WIDTH, 0));

        JLabel promptLabel = new JLabel("Whats happening?");
        promptLabel.setFont(new Font("SansSerif", Font.PLAIN, 32));
        promptLabel.setAlignmentX(LEFT_ALIGNMENT);

        JTextField titleField = new JTextField();
        titleField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        titleField.setPreferredSize(new Dimension(0, 42));
        titleField.setAlignmentX(LEFT_ALIGNMENT);
        installPlaceholder(titleField, "Title");

        JTextArea bodyArea = new JTextArea(12, 40);
        bodyArea.setLineWrap(true);
        bodyArea.setWrapStyleWord(true);
        installPlaceholder(bodyArea, "Body");

        doneButton.addActionListener(event -> {
            String title = getSubmittedText(titleField, "Title");
            String body = getSubmittedText(bodyArea, "Body");

            if (title.isBlank() || body.isBlank()) {
                return;
            }

            onDone.accept(new Post(title, body, Set.of()));
        });

        JScrollPane bodyScrollPane = new JScrollPane(bodyArea);
        bodyScrollPane.setPreferredSize(new Dimension(0, 210));
        bodyScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        bodyScrollPane.setAlignmentX(LEFT_ALIGNMENT);

        JPanel buttonRow = new JPanel(new BorderLayout());
        buttonRow.setOpaque(false);
        buttonRow.setAlignmentX(LEFT_ALIGNMENT);
        buttonRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, backButton.getPreferredSize().height));
        buttonRow.add(backButton, BorderLayout.WEST);
        buttonRow.add(doneButton, BorderLayout.EAST);

        formPanel.add(Box.createVerticalStrut(28));
        formPanel.add(promptLabel);
        formPanel.add(Box.createVerticalStrut(getTitleGapPixels()));
        formPanel.add(titleField);
        formPanel.add(Box.createVerticalStrut(24));
        formPanel.add(bodyScrollPane);
        formPanel.add(Box.createVerticalStrut(12));
        formPanel.add(buttonRow);

        JPanel centeredFormRow = new JPanel();
        centeredFormRow.setOpaque(false);
        centeredFormRow.setLayout(new BoxLayout(centeredFormRow, BoxLayout.X_AXIS));
        centeredFormRow.add(Box.createHorizontalGlue());
        centeredFormRow.add(formPanel);
        centeredFormRow.add(Box.createHorizontalGlue());

        add(centeredFormRow, BorderLayout.CENTER);
    }

    // Effects: displays light placeholder text in the title field until the user
    // focuses the field and starts entering their own text.
    private void installPlaceholder(JTextField field, String placeholderText) {
        field.setText(placeholderText);
        field.setForeground(PLACEHOLDER_COLOR);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                if (field.getText().equals(placeholderText) && field.getForeground().equals(PLACEHOLDER_COLOR)) {
                    field.setText("");
                    field.setForeground(INPUT_COLOR);
                }
            }

            @Override
            public void focusLost(FocusEvent event) {
                if (field.getText().isBlank()) {
                    field.setText(placeholderText);
                    field.setForeground(PLACEHOLDER_COLOR);
                }
            }
        });
    }

    // Effects: displays light placeholder text in the body field until the user
    // focuses the field and starts entering their own text.
    private void installPlaceholder(JTextArea area, String placeholderText) {
        area.setText(placeholderText);
        area.setForeground(PLACEHOLDER_COLOR);

        area.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                if (area.getText().equals(placeholderText) && area.getForeground().equals(PLACEHOLDER_COLOR)) {
                    area.setText("");
                    area.setForeground(INPUT_COLOR);
                }
            }

            @Override
            public void focusLost(FocusEvent event) {
                if (area.getText().isBlank()) {
                    area.setText(placeholderText);
                    area.setForeground(PLACEHOLDER_COLOR);
                }
            }
        });
    }

    // Effects: returns the user's input text unless the field still only contains
    // its placeholder value, in which case an empty string is returned.
    private String getSubmittedText(JTextField field, String placeholderText) {
        if (field.getForeground().equals(PLACEHOLDER_COLOR) && field.getText().equals(placeholderText)) {
            return "";
        }

        return field.getText().trim();
    }

    // Effects: returns the user's input text unless the field still only contains
    // its placeholder value, in which case an empty string is returned.
    private String getSubmittedText(JTextArea area, String placeholderText) {
        if (area.getForeground().equals(PLACEHOLDER_COLOR) && area.getText().equals(placeholderText)) {
            return "";
        }

        return area.getText().trim();
    }

    // Effects: returns the pixel spacing that most closely matches a 1 inch gap
    // below the create-post title on the current display.
    private int getTitleGapPixels() {
        int screenDpi = Toolkit.getDefaultToolkit().getScreenResolution();
        return (int) Math.round(screenDpi * TITLE_GAP_INCHES);
    }
}
