package pcd.ass02;


import javax.swing.*;

public class AnalysisController {
    private final AnalysisModel model;
    private final AnalysisView view;

    public AnalysisController(AnalysisModel model, AnalysisView view) {
        this.model = model;
        this.view = view;
        initListeners();
    }

    private void initListeners() {
        view.getSelectDirButton().addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = chooser.showOpenDialog(view.getSelectDirButton().getParent());
            if (result == JFileChooser.APPROVE_OPTION) {
                model.setSelectedDir(chooser.getSelectedFile());

                String fullPath = model.getSelectedDir().getPath();
                String displayPath = truncatePathStart(fullPath, 100);
                view.updateStatus("Selected " + displayPath);
            } else {
                view.updateStatus("Idle");
            }
        });

        view.getStartButton().addActionListener(e -> {
            if (model.getSelectedDir() != null) {
                startAnalysis();
            } else {
                view.updateStatus("Please select a folder");
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

    private void startAnalysis() {
        model.reset();
        view.updateStatus("Analyzing...");
        view.updateAnalyzedCount(0);
        view.updateDepsCount(0);
        view.updateHierarchy("");

        model.parseProject(
            report -> {
                StringBuilder hierarchy = model.getHierarchy(report);
                this.updateView(hierarchy);
            },
            () -> this.updateViewStatus("Analysis complete"),
            error -> this.updateViewStatus("Error: " + error.getMessage())
        );
    }

    public void updateViewStatus(String string) {
        SwingUtilities.invokeLater(() -> view.updateStatus(string));
    }

    public void updateView(StringBuilder hierarchy) {
        SwingUtilities.invokeLater(() -> {
            view.updateAnalyzedCount(model.getAnalyzedCount().get());
            view.updateDepsCount(model.getDepsCount().get());
            view.updateHierarchy(hierarchy.toString());
        });
    }
}
