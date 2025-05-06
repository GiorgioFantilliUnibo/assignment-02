package pcd.ass02;

import java.util.List;
import java.util.Set;

public class ClassDepsReport {
    private final String className;
    private final List<DependencyEntry> dependencies; 

    public ClassDepsReport(String className, List<DependencyEntry> dependencies) {
        this.className = className;
        this.dependencies = dependencies;
    }

    public String getClassName() { return className; }
    public List<DependencyEntry> getDependencies() { return dependencies; }

    public record DependencyEntry(String type, String context) {
        @Override
        public String toString() {
            return "type " + type + " (" + context + ")";
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Class: ").append(className).append("\n");
        for (DependencyEntry dep : dependencies) {
            sb.append(dep.toString()).append("\n");
        }
        return sb.toString();
    }
}
