package pcd.ass02;

import java.util.Set;
import java.util.stream.Collectors;


public class PackageDepsReport {
    private final String packageName;
    private final Set<ClassDepsReport> classReports;

    public PackageDepsReport(String packageName, Set<ClassDepsReport> classReports) {
        this.packageName = packageName;
        this.classReports = classReports;
    }

    public String getPackageName() { return packageName; }
    public Set<ClassDepsReport> getClassReports() { return classReports; }

    @Override
    public String toString(){
        return "\n\nPackage: " + packageName + "\n" + 
                classReports.stream().map(ClassDepsReport::toString).collect(Collectors.joining("\n"));
    }
}
