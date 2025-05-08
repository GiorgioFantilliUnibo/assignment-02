
package pcd.ass02;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.io.File;
import java.util.Iterator;

public class DependencyParser {

    public Flowable<ClassDepsReport> parseProject(File projectDir) {
        return Flowable.<File>create(emitter -> {
                Iterator<File> fileIterator = new RecursiveFileOpenIterator(projectDir);
                while (fileIterator.hasNext()) {
                    emitter.onNext(fileIterator.next());
                }
                emitter.onComplete();
            }, BackpressureStrategy.BUFFER)
                .filter(file -> file.isFile() && file.getName().endsWith(".java"))
                .flatMap(file -> Flowable.fromCallable(() -> {
                    CompilationUnit cu = StaticJavaParser.parse(file);
                    return new Visitor<Object>().visitAST(cu, null);
                }).onErrorResumeNext(e -> {
                    System.err.println("Error parsing " + file.getName() + ": " + e.getMessage());
                    return Flowable.empty();
                }))
                .subscribeOn(Schedulers.io());
    }

}
