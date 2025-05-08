
package pcd.ass02;

import java.util.List;

public class ClassDepsReport {
    private final String className;
    private final String packageName;
    private final List<DependencyEntry> dependencies;

    public ClassDepsReport(String className, String packageName, List<DependencyEntry> dependencies) {
        this.className = className;
        this.packageName = packageName;
        this.dependencies = dependencies;
    }

    public String getClassName() { return className; }
    public String getPackageName() { return packageName; }
    public List<DependencyEntry> getDependencies() { return dependencies; }

    public static class DependencyEntry {
        private final String type;
        private final String context;

        public DependencyEntry(String type, String context) {
            this.type = type;
            this.context = context;
        }

        public String getType() { return type; }
        public String getContext() { return context; }
    }
}
