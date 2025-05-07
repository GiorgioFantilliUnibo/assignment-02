
package pcd.ass02;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import io.vertx.core.*;
import pcd.ass02.ClassDepsReport.DependencyEntry;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DependencyAnalyserLib {
    private final Vertx vertx;
    private final JavaParser javaParser;

    public DependencyAnalyserLib(Vertx vertx) {
        this.vertx = vertx;
        this.javaParser = new JavaParser();
    }

    public Future<ClassDepsReport> getClassDependencies(File classSrcFile) {
        Promise<ClassDepsReport> promise = Promise.promise();
        vertx.fileSystem().readFile(classSrcFile.getAbsolutePath(), res -> {
            if (res.succeeded()) {
                try {
                    ParseResult<CompilationUnit> parseResult = javaParser
                            .parse(res.result().toString(StandardCharsets.UTF_8));
                    if (parseResult.isSuccessful()) {
                        CompilationUnit cu = parseResult.getResult().get();
                        List<DependencyEntry> dependencies = new ArrayList<>();
                        String className = extractClassName(cu);

                        new VoidVisitorAdapter<Void>() {
                            @Override
                            public void visit(PackageDeclaration n, Void arg) {
                                super.visit(n, arg);
                                dependencies.add(new DependencyEntry(n.getNameAsString(), "package decl"));
                            }

                            @Override
                            public void visit(ClassOrInterfaceDeclaration n, Void arg) {
                                super.visit(n, arg);
                                dependencies.add(new DependencyEntry(n.getNameAsString(), "class/int decl"));
                            }

                            @Override
                            public void visit(FieldDeclaration n, Void arg) {
                                super.visit(n, arg);
                                n.getVariables().forEach(v -> {
                                    dependencies.add(new DependencyEntry(v.getType().asString(), "field decl"));
                                });
                            }

                            @Override
                            public void visit(MethodDeclaration n, Void arg) {
                                super.visit(n, arg);
                                n.getParameters().forEach(p -> {
                                    dependencies.add(
                                            new DependencyEntry(p.getType().asString(), "method decl, param type"));
                                });
                                dependencies
                                        .add(new DependencyEntry(n.getType().asString(), "method decl, return type"));
                            }

                            @Override
                            public void visit(ObjectCreationExpr n, Void arg) {
                                super.visit(n, arg);
                                dependencies.add(new DependencyEntry(n.getType().asString(), "obj creation decl"));
                            }

                            @Override
                            public void visit(VariableDeclarator n, Void arg) {
                                super.visit(n, arg);
                                dependencies.add(new DependencyEntry(n.getType().asString(), "var decl"));
                            }

                            @Override
                            public void visit(TypeParameter n, Void arg) {
                                super.visit(n, arg);
                                n.getTypeBound().forEach(bound -> {
                                    dependencies.add(new DependencyEntry(bound.asString(), "type param bound"));
                                });
                            }

                            @Override
                            public void visit(ImportDeclaration n, Void arg) {
                                super.visit(n, arg);
                                if (!n.isAsterisk()) {
                                    var nameNode = n.getChildNodes().get(0); 
                                    var typeName = nameNode.toString();
                                    var packageNode = nameNode.getChildNodes().get(0); 
                                    String packageName = packageNode.toString();
                                    String context = "import package: " + packageName;
                                    dependencies.add(new DependencyEntry(typeName, context));
                                } else {
                                    var packageNode = n.getChildNodes().get(0); 
                                    String packageName = packageNode.toString();
                                    dependencies.add(new DependencyEntry(packageName, "import"));
                                }
                            }
                        }.visit(cu, null);
                        promise.complete(new ClassDepsReport(className, dependencies));
                    } else {
                        promise.fail("Parsing failed: " + parseResult.getProblems());
                    }
                } catch (Exception e) {
                    promise.fail("Parsing error: " + e.getMessage());
                }
            } else {
                promise.fail("File read error: " + res.cause().getMessage());
            }
        });
        return promise.future();
    }

    public Future<PackageDepsReport> getPackageDependencies(File packageSrcFolder) {
        Promise<PackageDepsReport> promise = Promise.promise();
        vertx.fileSystem().readDir(packageSrcFolder.getAbsolutePath(), ".*\\.java$", res -> {
            if (res.succeeded()) {
                List<String> filePaths = res.result().stream()
                        .map(File::new)
                        .filter(File::isFile) 
                        .map(File::getAbsolutePath)
                        .toList();
                if (filePaths.isEmpty()) {
                    promise.complete(new PackageDepsReport(packageSrcFolder.getName(), new HashSet<>()));
                } else {
                    List<Future<ClassDepsReport>> futures = filePaths.stream()
                            .map(path -> getClassDependencies(new File(path)))
                            .toList();

                    Future.all(futures).onComplete(cf -> {
                        if (cf.succeeded()) {
                            Set<ClassDepsReport> reports = cf.result().list().stream()
                                    .map(r -> (ClassDepsReport) r)
                                    .collect(Collectors.toSet());
                            promise.complete(new PackageDepsReport(packageSrcFolder.getName(), reports));
                        } else {
                            promise.fail(cf.cause());
                        }
                    });
                }
            } else {
                promise.fail(res.cause());
            }
        });
        return promise.future();
    }

    public Future<ProjectDepsReport> getProjectDependencies(File projectSrcFolder) {
        Promise<ProjectDepsReport> promise = Promise.promise();
        vertx.fileSystem().readDir(projectSrcFolder.getAbsolutePath(), res -> {
            if (res.succeeded()) {

                List<PackageDepsReport> reports = new ArrayList<>();

                Future<PackageDepsReport> currentFolderFuture = getPackageDependencies(projectSrcFolder);

                List<Future<ProjectDepsReport>> nestedFoldersfuture = res.result().stream()
                        .map(path -> new File(path))
                        .filter(File::isDirectory)
                        .map(this::getProjectDependencies)
                        .toList();

                currentFolderFuture.onComplete(cf -> {
                    if (cf.succeeded()) {
                        reports.add(0, cf.result());;
                    } else {
                        promise.fail(cf.cause());
                    }
                });

                Future.all(nestedFoldersfuture).onComplete(cf -> {
                    if (cf.succeeded()) {
                        List<ProjectDepsReport> projectsReports = cf.result().list();
                        projectsReports.stream().flatMap(r -> r.getPackageReports().stream()).forEach(reports::add);
                    } else {
                        promise.fail(cf.cause());
                    }
                });

                List<Future<?>> futures = new ArrayList<>();
                futures.add(currentFolderFuture);
                futures.addAll(nestedFoldersfuture);

                Future.all(futures).onComplete(cf -> {
                    if(cf.succeeded()){
                        promise.complete(new ProjectDepsReport(projectSrcFolder.getAbsolutePath(), reports));
                    } else {
                        promise.fail(cf.cause());
                    }
                });

            } else {
                promise.fail(res.cause());
            }
        });
        return promise.future();
    }

    private String extractClassName(CompilationUnit cu) {
        return cu.findFirst(ClassOrInterfaceDeclaration.class)
                .flatMap(c -> c.getFullyQualifiedName())
                .orElse("Unknown");
    }
}
