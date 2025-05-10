package pcd.ass02;

import java.io.File;
import java.util.Optional;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava3.core.BackpressureOverflowStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AnalysisModel {
    private Optional<File> selectedDir;
    private final AtomicInteger analyzedCount = new AtomicInteger(0);
    private final AtomicInteger depsCount = new AtomicInteger(0);
    private final StringBuilder hierarchy;
    private final DependencyParser parser;
    private final Map<String, Integer> packageClassCount;
    private final Set<String> printedPackages;

    public AnalysisModel(DependencyParser parser) {
        hierarchy = new StringBuilder();
        this.parser = parser;
        this.packageClassCount = new HashMap<>();
        this.printedPackages = new HashSet<>();
    }

    public File getSelectedDir() {
        return selectedDir.orElseThrow(IllegalStateException::new);
    }

    public void setSelectedDir(File selectedDir) {
        this.selectedDir = Optional.of(selectedDir);
    }

    public AtomicInteger getAnalyzedCount() {
        return analyzedCount;
    }

    public AtomicInteger getDepsCount() {
        return depsCount;
    }

    public void reset() {
        analyzedCount.set(0);
        depsCount.set(0);
        hierarchy.setLength(0);
        packageClassCount.clear();
        printedPackages.clear();
    }

    public void parseProject(Consumer<ClassDepsReport> onNext, Action onComplete,  Consumer<Throwable> onError) {
        Flowable<ClassDepsReport> classReports =  parser.parseProject(this.selectedDir.orElseThrow(IllegalStateException::new));

        classReports
                .onBackpressureBuffer(1000, () -> System.err.println("Buffer overflow"), BackpressureOverflowStrategy.ERROR)
                .observeOn(Schedulers.computation())
                .doOnNext(report -> {
                    this.getAnalyzedCount().incrementAndGet();
                    this.getDepsCount().addAndGet(report.getDependencies().size());
                })
                .subscribe(
                        onNext,
                        onError,
                        onComplete
                );
    }

    public StringBuilder getHierarchy(ClassDepsReport report) {
        String packageName = report.getPackageName().isEmpty() ? "default" : report.getPackageName();

        int classIndex = packageClassCount.compute(packageName, (k, v) -> v == null ? 1 : v + 1);

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

        int packageDepth = packageName.equals("default") ? 0 : packageName.split("\\.").length;
        String classIndent = "  ".repeat(packageDepth + 3);
        for (ClassDepsReport.DependencyEntry dep : report.getDependencies()) {
            if (dep.getContext().equals("class/int decl")) {
                hierarchy.append(classIndent).append("Class: ").append(dep.getType())
                        .append(" (").append(dep.getContext()).append(")\n");
                break;
            }
        }

        String depIndent = "  ".repeat(packageDepth + 5);
        for (ClassDepsReport.DependencyEntry dep : report.getDependencies()) {
            if (!dep.getContext().equals("class/int decl") && !dep.getContext().equals("package decl") && !dep.getContext().equals("import")) {
                hierarchy.append(depIndent).append(" ").append(dep.getType())
                        .append(" (").append(dep.getContext()).append(")\n");
            }
        }

        return hierarchy;
    }
}
