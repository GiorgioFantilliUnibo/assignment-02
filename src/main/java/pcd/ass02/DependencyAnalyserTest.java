package pcd.ass02;

import io.vertx.core.Vertx;
import pcd.ass02.ClassDepsReport.DependencyEntry;

import java.io.File;
import java.util.stream.Collectors;

public class DependencyAnalyserTest {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        DependencyAnalyserLib analyser = new DependencyAnalyserLib(vertx);

        // Test getClassDependencies (MyClass.java)
        File classFile = new File("src/main/java/pcd/ass02/MyClass.java");
        analyser.getClassDependencies(classFile)
            .onSuccess(System.out::println)
            .onFailure(err -> System.err.println("Class Error: " + err.getMessage()));

        // Test getPacketDependencies (.java files in src/main/java/pcd/ass02 only)
        File packageFolder = new File("src/main/java/pcd/ass02");
        analyser.getPackageDependencies(packageFolder)
            .onSuccess(System.out::println)
            .onFailure(err -> System.err.println("Package Error: " + err.getMessage()));

        // Test getProjectDependencies (entire src/main/java)
        File projectFolder = new File("src/main/java");
        analyser.getProjectDependencies(projectFolder)
            .onSuccess(System.out::println)
            .onFailure(err -> System.err.println("Project Error: " + err.getMessage()));

        //Close Vert.x after a delay to allow async operations to complete
        vertx.setTimer(5000, id -> vertx.close());
    }
}
