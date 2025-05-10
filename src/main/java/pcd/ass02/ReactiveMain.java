// package pcd.ass02;

// import java.io.File;
// import java.io.FileNotFoundException;
// import java.util.Arrays;
// import java.util.Iterator;
// import java.util.List;
// import java.util.NoSuchElementException;
// import java.util.Optional;
// import java.util.stream.Stream;

// import com.github.javaparser.StaticJavaParser;
// import com.github.javaparser.ast.CompilationUnit;
// import com.github.javaparser.ast.ImportDeclaration;
// import com.github.javaparser.ast.PackageDeclaration;
// import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
// import com.github.javaparser.ast.body.FieldDeclaration;
// import com.github.javaparser.ast.body.MethodDeclaration;
// import com.github.javaparser.ast.body.VariableDeclarator;
// import com.github.javaparser.ast.expr.ObjectCreationExpr;
// import com.github.javaparser.ast.type.TypeParameter;
// import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

// import io.reactivex.rxjava3.core.Flowable;
// import io.reactivex.rxjava3.schedulers.Schedulers;

// public class ReactiveMain {

//     public static void main(String[] args) throws Exception {

//         File file = new File("src/main/java/pcd/ass02");

//         // CompilationUnit cu = StaticJavaParser.parse(file);

//         // System.out.println(new Visitor<Object>().visitAST(cu, null));

//         Flowable.fromIterable(new Iterable<String>() {

//             @Override
//             public Iterator<String> iterator() {
//                 return new Iterator<>() {

//                     // private Iterator<File> files = Arrays.asList(Optional.ofNullable(file.listFiles(File::isFile)).orElse(new File[] { file })).iterator();

//                     private Iterator<File> files = new RecursiveFileOpenIterator(file);

//                     @Override
//                     public boolean hasNext() {
//                         return files.hasNext();
//                     }

//                     @Override
//                     public String next() {
//                         File next = files.next();
//                         while(next.getName().equals("Prova.java") || next.getName().equals("RecursiveFileOpenIterator.java")){
//                             next = files.next();
//                         }
                            
//                         if (next.isDirectory()){
//                             return "Directory: " + next.getName();
//                         } else {
//                             try {
//                                 return new Visitor<Object>().visitAST(StaticJavaParser.parse(next), null);
//                             } catch (FileNotFoundException e) {
//                                 e.printStackTrace();
//                                 throw new NoSuchElementException();
//                             } catch (Exception e) {
//                                 System.out.println("FileName: " + next.getName());
//                                 e.printStackTrace();
//                                 throw new NoSuchElementException();
//                             }
//                         }
//                     }
//                 };
//             }
//         }).subscribeOn(Schedulers.io())
//         .observeOn(Schedulers.single())
//         .subscribe(System.out::println, Throwable::printStackTrace);

//         try {
//             Thread.sleep(5000);
//         } catch (InterruptedException ex) {
//             Thread.currentThread().interrupt();
//         }
//         // Iterator<File> files = Arrays.asList(Optional.ofNullable(file.listFiles()).orElse(new File[] { file }))
//         //         .iterator();
//         // System.out.println(new Visitor<Object>().visitAST(StaticJavaParser.parse(files.next()), null));;
//     }

// }
