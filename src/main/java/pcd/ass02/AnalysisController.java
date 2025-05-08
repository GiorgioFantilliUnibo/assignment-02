package pcd.ass02;

import io.reactivex.rxjava3.core.BackpressureOverflowStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import javax.swing.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AnalysisController {
    private final AnalysisModel model;
    private final AnalysisView view;
    private final DependencyParser parser;
    private final Map<String, Integer> packageClassCount;
    private final Set<String> printedPackages;

    public AnalysisController(AnalysisModel model, AnalysisView view, DependencyParser parser) {
        this.model = model;
        this.view = view;
        this.parser = parser;
        this.packageClassCount = new HashMap<>();
        this.printedPackages = new HashSet<>();
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
        packageClassCount.clear();
        printedPackages.clear();
        view.updateStatus("Analyzing...");
        view.updateAnalyzedCount(0);
        view.updateDepsCount(0);
        view.updateHierarchy("");

        Flowable<ClassDepsReport> classReports = parser.parseProject(model.getSelectedDir());

        classReports
                .onBackpressureBuffer(1000, () -> System.err.println("Buffer overflow"), BackpressureOverflowStrategy.DROP_OLDEST)
                .observeOn(Schedulers.computation())
                .doOnNext(report -> {
                    model.getAnalyzedCount().incrementAndGet();
                    model.getDepsCount().addAndGet(report.getDependencies().size());

                    String packageName = report.getPackageName().isEmpty() ? "default" : report.getPackageName();

                    // Track class count per package
                    int classIndex = packageClassCount.compute(packageName, (k, v) -> v == null ? 1 : v + 1);

                    // Build hierarchy text
                    StringBuilder hierarchy = model.getHierarchy();

                    // Handle package hierarchy
                    if (classIndex == 1) {
                        String[] packageParts = packageName.equals("default") ? new String[]{"default"} : packageName.split("\\.");
                        String currentPackage = "";
                        for (int i = 0; i < packageParts.length; i++) {
                            currentPackage = currentPackage.isEmpty() ? packageParts[i] : currentPackage + "." + packageParts[i];
                            if (!printedPackages.contains(currentPackage)) {
                                String indent = "    ".repeat(i);
                                hierarchy.append(indent).append("> Package: ").append(packageParts[i]).append("\n");
                                printedPackages.add(currentPackage);
                            }
                        }
                    }

                    // Add class
                    int packageDepth = packageName.equals("default") ? 0 : packageName.split("\\.").length;
                    String classIndent = "  ".repeat(packageDepth + 3);
                    for (ClassDepsReport.DependencyEntry dep : report.getDependencies()) {
                        if (dep.getContext().equals("class/int decl")) {
                            hierarchy.append(classIndent).append("Class: ").append(dep.getType())
                                    .append(" (").append(dep.getContext()).append(")\n");
                            break;
                        }
                    }

                    // Add dependencies
                    String depIndent = "  ".repeat(packageDepth + 5);
                    for (ClassDepsReport.DependencyEntry dep : report.getDependencies()) {
                        if (!dep.getContext().equals("class/int decl") && !dep.getContext().equals("package decl") && !dep.getContext().equals("import")) {
                            hierarchy.append(depIndent).append(" ").append(dep.getType())
                                    .append(" (").append(dep.getContext()).append(")\n");
                        }
                    }

                    // Update GUI on Swing EDT
                    SwingUtilities.invokeLater(() -> {
                        view.updateAnalyzedCount(model.getAnalyzedCount().get());
                        view.updateDepsCount(model.getDepsCount().get());
                        view.updateHierarchy(hierarchy.toString());
                    });
                })
                .subscribe(
                        report -> { },
                        error -> SwingUtilities.invokeLater(() -> view.updateStatus("Error: " + error.getMessage())),
                        () -> SwingUtilities.invokeLater(() -> view.updateStatus("Analysis complete"))
                );
    }
}
