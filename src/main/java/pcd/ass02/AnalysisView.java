package pcd.ass02;

import javax.swing.*;
import java.awt.*;

public class AnalysisView {
    private final JFrame frame;
    private final JButton selectDirButton;
    private final JButton startButton;
    private final JLabel statusLabel;
    private final JLabel analyzedCountLabel;
    private final JLabel depsCountLabel;
    private final JTextArea hierarchyArea;
    private AnalysisController controller;

    public AnalysisView() {
        this.controller = controller;

        frame = new JFrame("Dependency Analyser");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(800, 600));
        frame.setLayout(new BorderLayout(10, 10));

        // Control panel (top)
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectDirButton = new JButton("Select Folder");
        startButton = new JButton("Start Analysis");
        controlPanel.add(selectDirButton);
        controlPanel.add(startButton);

        this.initListeners();

        // Status panel (top, below controls)
        JPanel statusPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        statusLabel = new JLabel("Status: Idle");
        analyzedCountLabel = new JLabel("Classes/Interfaces Analyzed: 0");
        depsCountLabel = new JLabel("Dependencies Found: 0");
        statusPanel.add(statusLabel);
        statusPanel.add(analyzedCountLabel);
        statusPanel.add(depsCountLabel);
        statusPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Hierarchy panel (center)
        hierarchyArea = new JTextArea();
        hierarchyArea.setEditable(false);
        hierarchyArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(hierarchyArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Dependency Hierarchy"));

        // Assemble frame
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(controlPanel, BorderLayout.NORTH);
        topPanel.add(statusPanel, BorderLayout.CENTER);
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        frame.setVisible(true);
    }

    private void initListeners(){
        selectDirButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            int result = chooser.showOpenDialog(selectDirButton.getParent());
            if (result == JFileChooser.APPROVE_OPTION) {
                controller.setSelectedDir(chooser.getSelectedFile());

                String fullPath = chooser.getSelectedFile().getPath();
                String displayPath = truncatePathStart(fullPath, 100);
                this.updateStatus("Selected " + displayPath);
            } else {
                this.updateStatus("Idle");
            }
        });

        startButton.addActionListener(e -> {
            if (!controller.startAnalysis()) {
                this.updateStatus("Please select a folder");
            }
        });
    }

    private String truncatePathStart(String path, int maxLength) {
        if (path == null) {
            return "";
        }
        if (path.length() <= maxLength) {
            return path;
        }
        return "..." + path.substring(path.length() - (maxLength - 3));
    }

    public void setController(AnalysisController controller){
        this.controller = controller;
    }

    public JButton getSelectDirButton() {
        return selectDirButton;
    }

    public JButton getStartButton() {
        return startButton;
    }

    public void updateStatus(String message) {
        statusLabel.setText("Status: " + message);
    }

    public void updateAnalyzedCount(int count) {
        analyzedCountLabel.setText("Classes/Interfaces Analyzed: " + count);
    }

    public void updateDepsCount(int count) {
        depsCountLabel.setText("Dependencies Found: " + count);
    }

    public void updateHierarchy(String hierarchy) {
        hierarchyArea.setText(hierarchy);
        hierarchyArea.setCaretPosition(0);
    }
}
