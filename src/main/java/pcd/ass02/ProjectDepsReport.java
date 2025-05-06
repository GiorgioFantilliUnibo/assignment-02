package pcd.ass02;

import java.util.Set;


public class ProjectDepsReport {
    private final String projectPath;
    private final Set<PackageDepsReport> packageReports;

    public ProjectDepsReport(String projectPath, Set<PackageDepsReport> packageReports) {
        this.projectPath = projectPath;
        this.packageReports = packageReports;
    }

    public String getProjectPath() { return projectPath; }
    public Set<PackageDepsReport> getPackageReports() { return packageReports; }
}
