package pcd.ass02;

import java.util.Set;


public class PackageDepsReport {
    private final String packageName;
    private final Set<ClassDepsReport> classReports;

    public PackageDepsReport(String packageName, Set<ClassDepsReport> classReports) {
        this.packageName = packageName;
        this.classReports = classReports;
    }

    public String getPackageName() { return packageName; }
    public Set<ClassDepsReport> getClassReports() { return classReports; }
}
