package pcd.ass02;

import java.util.List;
import java.util.stream.Collectors;


public class ProjectDepsReport {
    private final String projectPath;
    private final List<PackageDepsReport> packageReports;

    public ProjectDepsReport(String projectPath, List<PackageDepsReport> packageReports) {
        this.projectPath = projectPath;
        this.packageReports = packageReports;
    }

    public String getProjectPath() { return projectPath; }
    public List<PackageDepsReport> getPackageReports() { return packageReports; }

    @Override
    public String toString(){
        return projectPath + "\n" + 
            packageReports.stream().map(PackageDepsReport::toString).collect(Collectors.joining("\n")) + "\n";
    }

}
