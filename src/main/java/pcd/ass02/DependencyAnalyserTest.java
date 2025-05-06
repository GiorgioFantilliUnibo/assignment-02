package pcd.ass02;

import io.vertx.core.Vertx;

import java.io.File;

public class DependencyAnalyserTest {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        DependencyAnalyserLib analyser = new DependencyAnalyserLib(vertx);

        File classFile = new File("src/main/java/pcd/ass02/MyClass.java");
        analyser.getClassDependencies(classFile)
            .onSuccess(System.out::println)
            .onFailure(err -> System.err.println("Class Error: " + err.getMessage()));

        File packageFolder = new File("src/main/java/pcd/ass02");
        analyser.getPackageDependencies(packageFolder)
            .onSuccess(report -> {
                System.out.println("\nPackage: " + report.getPackageName());
                report.getClassReports().forEach(System.out::println);
            })
            .onFailure(err -> System.err.println("Package Error: " + err.getMessage()));

        // // Test getProjectDependencies (entire src/main/java)
        // File projectFolder = new File("src/main/java");
        // analyser.getProjectDependencies(projectFolder)
        //     .onSuccess(report -> {
        //         System.out.println("\nProject: " + report.projectPath());
        //         report.packageReports().forEach(p -> {
        //             System.out.println("  Package: " + p.packageName());
        //             p.classReports().forEach(r ->
        //                 System.out.println("    Class: " + r.className() + ", Deps: " + r.dependencies()));
        //         });
        //     })
        //     .onFailure(err -> System.err.println("Project Error: " + err.getMessage()));

        // Close Vert.x after a delay to allow async operations to complete
        vertx.setTimer(5000, id -> vertx.close());
    }
}
