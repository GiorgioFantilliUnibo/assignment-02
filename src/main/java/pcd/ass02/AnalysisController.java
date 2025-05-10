package pcd.ass02;


import javax.swing.*;
import java.io.File;
import java.util.Optional;

public class AnalysisController {
    private final AnalysisModel model;
    private final AnalysisView view;

    public AnalysisController(AnalysisModel model, AnalysisView view) {
        this.model = model;
        this.view = view;
        this.view.setController(this);
    }

    public boolean startAnalysis() {
        model.reset();
        view.updateStatus("Analyzing...");
        view.updateAnalyzedCount(0);
        view.updateDepsCount(0);
        view.updateHierarchy("");

        try {
            model.parseProject(
                    report -> {
                        StringBuilder hierarchy = model.getHierarchy(report);
                        this.updateView(hierarchy);
                    },
                    () -> this.updateViewStatus("Analysis complete"),
                    error -> this.updateViewStatus("Error: " + error.getMessage())
            );
        } catch (IllegalStateException e) {
            return false;
        }
        return true;
    }

    public void setSelectedDir(File selectedDir) {
        this.model.setSelectedDir(selectedDir);
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
