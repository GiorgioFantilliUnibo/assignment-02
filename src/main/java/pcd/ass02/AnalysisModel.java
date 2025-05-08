package pcd.ass02;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

public class AnalysisModel {
    private File selectedDir;
    private final AtomicInteger analyzedCount = new AtomicInteger(0);
    private final AtomicInteger depsCount = new AtomicInteger(0);
    private final StringBuilder hierarchy;

    public AnalysisModel() {
        hierarchy = new StringBuilder();
    }

    public File getSelectedDir() {
        return selectedDir;
    }

    public void setSelectedDir(File selectedDir) {
        this.selectedDir = selectedDir;
    }

    public AtomicInteger getAnalyzedCount() {
        return analyzedCount;
    }

    public AtomicInteger getDepsCount() {
        return depsCount;
    }

    public StringBuilder getHierarchy() {
        return hierarchy;
    }

    public void reset() {
        analyzedCount.set(0);
        depsCount.set(0);
        hierarchy.setLength(0);
    }
}
